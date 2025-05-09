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

/**
\file

\brief class which provides the RST-Format support

 */
#include "Rst.h"
#include "Fixed.h"

namespace pearlrt {
    Rst::Rst() {
        rstVoidPointer = NULL;
        rstLength = 0;
        //printf("Rst::Rst this=%p  Ptr=%p size=%d\n", this, rstVoidPointer, rstLength);
    }

    void Rst::rst(void * rstPointer, size_t len) {
        //printf("Rst::rst Ptr=%p size=%d\n", rstPointer, len);
        rstVoidPointer = rstPointer;
        rstLength = len;
        /* is in Fixed.h */
        assignInt32ToFixedViaVoidPointer(rstVoidPointer, rstLength, 0);
    }

    bool Rst::updateRst(Signal * s) {
        //printf("Rst::updateRst ptr=%p size=%d\n", rstVoidPointer, rstLength);
        if (rstVoidPointer != NULL) {
            if (s->whichRST() < 100) {
                // internal signals are not catchable
                return false;
            }
            /* is in Fixed.h */
            //printf("Rst.cc updateRst  ... rstLength = %d newValue=%d\n", rstLength,s->whichRST());
            assignInt32ToFixedViaVoidPointer(rstVoidPointer, rstLength,
                    s->whichRST());
            return true;
        }

        return false;
    }

}
