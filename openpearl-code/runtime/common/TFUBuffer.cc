/*
 [A "BSD license"]
 Copyright (c) 2017 Rainer Mueller
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions
 are met:

 1. Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
 3. The name of the author may not be used to endorse or promote products
    derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

#include <string.h>    // memcpy, memset


#include "TFUBuffer.h"
#include "Sink.h"
#include "Source.h"
#include "SystemDationNB.h"
#include "Signals.h"
#include "Log.h"

namespace pearlrt {

   void TFUBuffer::flushRecord() {
      int trailingPaddingCharacters = 0;
      int resultingSize=0;
      static /* const */ char nl = '\n';
      if (isAlphicForward) {
         for (int i=sizeOfRecord-1; i>=0; i--) {
             if (record[i] == ' ') {
                trailingPaddingCharacters ++;
             } else {
                break;
             }
         }
         if (trailingPaddingCharacters) {
            record[sizeOfRecord-trailingPaddingCharacters] = '\n';
            resultingSize = sizeOfRecord - trailingPaddingCharacters +1;
         }
         system->dationWrite(record, resultingSize);
         if (trailingPaddingCharacters==0) {
           system->dationWrite(&nl, 1);
         }
      } else {
         system->dationWrite(record, sizeOfRecord);
      }
      readWritePointer = 0;
      prepare();
   }

   //void TFUBuffer::readRecord(bool untilNL) {
   void TFUBuffer::readRecord() {
      size_t i;
      bool untilNL = isAlphicForward;
      char ch = 0;
      bool nlFound = false;
      bool nonPaddingCharacterFound = false;

      if (untilNL) {
         for (i = 0; i < sizeOfRecord && ch != '\n'; i++) {
            system->dationRead(&ch, 1);
            record[i] = ch;

            if (ch == '\n') {
               nlFound = true;
               break;   // leave loop
            }
         }

         // if the NL was detected earlier the line is accepted
         // if not - the NL must be the next character
         while (!nlFound) {
            system->dationRead(&ch, 1);

            if (ch != ' ' && ch != '\n') {
               nonPaddingCharacterFound = true;
            }

            if (ch == '\n') {
               nlFound = true;
            }
         }

         if (nonPaddingCharacterFound) {
            Log::error("TFUBuffer: input record too long");
            throw theDationTFURecordSignal;
         }

         for (i++; i < sizeOfRecord; i++) {
            record[i] = paddingElement;
         }

         containsData = true;
//printf("readRecord: %02x %02x %02x ...\n", record[0], record[1], record[2]);
      } else {
         system->dationRead(record, sizeOfRecord);
         containsData = true;
      }

//      readWritePointer = 0;
   }
   void TFUBuffer::setAlphicForward(bool isAlphicForward) {
      this->isAlphicForward = isAlphicForward;
   }

   TFUBuffer::TFUBuffer() {
      this->isAlphicForward = false;
      sizeOfRecord = 0;
      record = NULL;
      system = NULL;
      readWritePointer = 0;
      containsData = false;
   }

   void TFUBuffer::markEmpty() {
      forgetUnGetChar();
      readWritePointer = 0;
      containsData = false;
   }

   void TFUBuffer::setSystemDation(SystemDationNB * sys) {
      system = sys;
   }

   void TFUBuffer::setupRecord(int recLen, char * tfuRecord,
                               int p) {
      sizeOfRecord = recLen;
      record = tfuRecord;
      paddingElement = p;
   }

   void TFUBuffer::prepare() {
      size_t i;
//printf("TFUBuffer::prepare\n");

      for (i = 0; i < sizeOfRecord; i++) {
         record[i] = paddingElement;
      }

//      readWritePointer = 0;
      containsData = false;
   }

   bool TFUBuffer::isUsed() {
      return record;
   }

   bool TFUBuffer::isNotEmpty() {
      return (containsData);
   }

   void TFUBuffer::read(void * data, size_t n) {
//printf("TFU::read: isUsed()  %d -- isEmpty: %d\n", isUsed(), !isNotEmpty());
      if (isUsed()) {
         if (!isNotEmpty()) {
//            readRecord(paddingElement); // if padding element is 0 --> DationRW
            readRecord(); // if padding element is 0 --> DationRW
         }

         if (sizeOfRecord - readWritePointer >= n) {
            memcpy(data, &record[readWritePointer], n);
            readWritePointer += n;
         } else {
            containsData = false;
            Log::error("read over end of TFU buffer");
            throw theInternalDationSignal;
         }
      } else {
         system->dationRead(data, n);
      }
   }

   void TFUBuffer::write(void * data, size_t n) {
      if (isUsed()) {
         if (!isNotEmpty()) {
            prepare(); // if padding element is 0 --> DationRW
         }

         if (readWritePointer + n > sizeOfRecord ) {
            Log::error("TFUBuffer: record overflow");
            throw theInternalDationSignal; 
         }

         memcpy(&record[readWritePointer], data, n);
         readWritePointer += n;
         containsData = true;
      } else {
         system->dationWrite(data, n);
      }
   }

   void TFUBuffer::setPosition(int newPos) {
      readWritePointer = newPos;
   }

   void TFUBuffer::putChar(char c) {

      if (isUsed()) {
         if (readWritePointer + 1 > sizeOfRecord ) {
            Log::error("TFUBuffer: record overflow");
            throw theInternalDationSignal; 
         }
         record[readWritePointer++] = c;
         containsData = true;
      } else {
         system->dationWrite(&c, 1);
      }
   }

   char TFUBuffer::realGetChar() {
      char c;

      if (isUsed()) {
         if (!containsData) {
            // until NL since this method is used only in DationPG
            readRecord();
         }

         if (readWritePointer < sizeOfRecord) {
            c = record[readWritePointer++];
         } else {
            containsData = false;
         }
      } else {
         try {
            system->dationRead(&c, 1);
         } catch (Signal & s)  {
            if (s.whichRST() == theTerminateRequestSignal.whichRST()) {
               throw;
            } else if (s.whichRST() == theInternalTaskSignal.whichRST()) {
	       throw;
            }
            
            throw theNoMoreCharactersSignal;
         }
      }

      return c;
   }


}
