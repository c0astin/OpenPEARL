/*
 [A BSD license"]
 Copyright (c) 2012-2017 Rainer Mueller
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

/**
\file

\brief Data type REF CHARACTER

This class provides the operations of the data type ref char.

*/

#include "RefChar.h"
#include "Log.h"

namespace pearlrt {
   void RefCharacter::dump() const {
      printf("RefCharacter: max=%zu current=%zu data=",max,current);
      for (size_t i=0; i<current; i++) {
          if (data[i] >= ' ') printf("%c",data[i]);
          else printf("(%02x)",data[i]);
      }
      printf("\n");
   }

   RefCharacter::RefCharacter() {
      max = 0;
      current = 0;
      data = 0;
      charIsINV = false;
   }

   RefCharacter::RefCharacter(char * s) {
      max = strlen(s);
      current = max;
      data = s;
      charIsINV = true;
   }

   void RefCharacter::rewind() {
      current = 0;
   }

   void RefCharacter::setWork(void * s, size_t len) {
       data=(char*)s;
       max = len;
       current=len;
   }

   char RefCharacter::getCharAt(size_t index) {
      // size-t is unsigned; max -1 become very large if max = 0
      // if index was given wrong (negative) the other clause  works
      if (index > max - 1 || index + 1 > max) {
         throw theCharacterIndexOutOfRangeSignal;
      }

      return data[index];
   }

   char RefCharacter::getNextChar() {
      char result;
      if (current + 1 > max) {
         throw theCharacterTooLongSignal;
      }
      result = data[current];
      current ++;
      return result;
   }

   char * RefCharacter::getCstring() {
      if (current + 1 > max) {
         throw theCharacterTooLongSignal;
      }

      data[current] = '\0';
      return data;
   }

   void RefCharacter::fill() {
      if (charIsINV) {
          Log::error("REF CHAR: CHAR variable is INV (fill)");
          throw theCharIsINVSignal;
      }

      for (size_t i = current; i < max; i++) {
         data[i] = ' ';
      }
   }

   void RefCharacter::add(const RefCharacter& rhs) {
      if (charIsINV) {
          Log::error("REF CHAR: CHAR variable is INV (add RC)");
          throw theCharIsINVSignal;
      }

      size_t newCurrent = current + rhs.current;

      if (newCurrent > max) {
         throw theCharacterTooLongSignal;
      }

      size_t j = 0;

      for (size_t i = current; i < newCurrent; i++) {
         data[i] = rhs.data[j++];
      }

      current = newCurrent;
   }

   void RefCharacter::add(const char rhs) {
      if (charIsINV) {
          Log::error("REF CHAR: CHAR variable is INV (add char)");
          throw theCharIsINVSignal;
      }

      if (current + 1 > max) {
         throw theCharacterTooLongSignal;
      }

      data[current++] = rhs;
   }

   void RefCharacter::add(const char * rhs) {
      if (charIsINV) {
          Log::error("REF CHAR: CHAR variable is INV (add char*)");
          throw theCharIsINVSignal;
      }

      while (*rhs) {
         add(*rhs);
         rhs++;
      }
   }

   Fixed<31> RefCharacter::getMax() {
      return Fixed<31>(max);
   }

   Fixed<31> RefCharacter::getCurrent() {
      return Fixed<31>(current);
   }

   char * RefCharacter::getDataPtr() const {
       return data;
   }

   void RefCharacter::setCurrent(size_t newCurrent) {
      if (newCurrent >= max) {
         throw theCharacterTooLongSignal;
      }
      current = newCurrent;
      return;
   }
}
