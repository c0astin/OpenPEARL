/*
 [A "BSD license"]
 Copyright (c) 2012-2013 Holger Koelle
 Copyright (c) 2014-2016 Rainer Mueller
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

#ifndef STDIN_INCLUDED
#define STDIN_INCLUDED


/**
\file

\brief generic non-basic systemdation class for reading
       from standard stream stdin
*/

#include "InterruptableSystemDationNB.h"
#include "Mutex.h"
#include "Character.h"
#include "RefChar.h"
#include <unistd.h>

namespace pearlrt {
   /**
   \addtogroup io_linux_driver
   @{
   */

   /**
   \brief generic non-basic systemdation class

   With this class it is possible to access linux input stream
   stdin.

   Usage:
   \verbatim
   SYSTEM;
      stdIn  : StdIn;
   PROBLEM;
      SPC stdIn DATION SYSTEM IN ALL;

   \endverbatim

   */
   class StdIn: public InterruptableSystemDationNB {

   private:
      /**
      mutex for  class data
      */
      Mutex mutex;

      /** access capabilities */
      int cap;

      /** flag, whether device is in use */
      bool inUse;

      /**
       number of files which may be simultaneously opened on this
       dation
      */
      int capacity;

      /**
      File* to use
      */
      FILE* fp;

      static bool _isDefined;

   public:

      /**
       Constructor to setup the system device

       PEARL attributes: FORWARD IN or OUT ALPHIC or type

      */
      StdIn();

      /**
         return capabilities of the folder objects

         The capabilities are marked be using the flags from
         Dation. The meaning of the flags are modified.
         E.g IN is means that the folder supports IN.


         \returns capabilities of the real folder
      */
      int capabilities();

      /**
       open method

       Identify the requested StdIn object and return a
       pointer to this object.

      \param fileName   the file name as C string (must be NULL)
      \param openParams the PEARL parameters for file open

      \returns pointer to working object to do the subsequent
                  dation operations.

      \throws OpenFailedSignal in case of errors
      \throws DationParamSignal in case of errors
      */
      StdIn* dationOpen(const RefCharacter * fileName, int openParams);

      /**
       close method.

       This method is empty.

      \param closeParams actions to be done at close (PRM, CAN)
      \throws CloseFailedSignal in case of errors

      */
      void dationClose(int closeParams);

      /**
      read method

       This method is empty.
       All operations are in the DiscFile::dationClose() method.

      \param destination target area for the read bytes
      \param size number of bytes to read

      \throws ReadingFailedSignal in case of read errors

      */
      void dationRead(void * destination, size_t size);

      /**
      write method

       This method is empty.

      \param destination source area for the bytes to be written
      \param size number of bytes to write

      \throws DationNotSupportedSignal in any case

      */
      void dationWrite(void * destination, size_t size);

      /**
       send one character back to the input.

      \param c the character which shall returned to the input

      \throws * depends an the implementation of the SystemDationNB device
      */
      void dationUnGetChar(const char c);


      /**
      translate newline

      this is empty since linux uses \n for newline

      \param doNewLineTranslation enbale/disable the translation
             (has no effect)
      */
      void translateNewLine(bool doNewLineTranslation);

      /**
      release dation mutex

      This method is required for proper application termination 
      with Console device. 
      The Console waits always on read for data ion StdIn. In case of
      termination of the last task - we must get the mutex of stdin 
      unlocked, or an error message for destroy a locked mutex will appear
      */
      void abortRead();
      

   };
   /** @} */
}
#endif
