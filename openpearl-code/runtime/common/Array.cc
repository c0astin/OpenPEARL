/*
 [A "BSD license"]
 Copyright (c) 2016 Rainer Mueller
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

\brief array utility functions

\author R. Mueller

*/

#include "Array.h"
#include "Signals.h"
#include "Fixed.h"
#include "Log.h"


namespace pearlrt {

   template<>
   size_t ArrayDescriptor<0>::_offset(Fixed<31> index, va_list args) {
      size_t offset = 0;
      int i;

      for (i = 0; i < dim; i++) {
         if (index.x < lim[i].low) {
            Log::error("array index #%d out of bounds (%d) [%d,%d])",
                       i, (int)index.x,
                       lim[i].low, lim[i].high);
            throw theArrayIndexOutOfBoundsSignal;
         } else if (index.x > lim[i].high) {
            Log::error("array index #%d out of bounds (%d) [%d,%d])",
                       i, (int)index.x,
                       lim[i].low, lim[i].high);
            throw theArrayIndexOutOfBoundsSignal;
         } else {
            offset += (index.x - lim[i].low) *
                      lim[i].size;
         }

         index = va_arg(args, Fixed<31>);
      }

      return offset;
   }

   template<>
   Fixed<31> ArrayDescriptor<0>::lwb(Fixed<31> x) {
      if (x.x > 0 && x.x <= dim) {
         return Fixed<31>(lim[x.x - 1].low);
      }

      Log::error("low: index %d out of range [1,%d]",
                 (int)x.x, dim);
      throw theArrayIndexOutOfBoundsSignal;
   }

   template<>
   Fixed<31> ArrayDescriptor<0>::upb(Fixed<31> x) {
      if (x.x > 0 && x.x <= dim) {
         return Fixed<31>(lim[x.x - 1].high);
      }

      Log::error("upb: index %d out of range [1,%d]",
                 (int)x.x, dim);
      throw theArrayIndexOutOfBoundsSignal;
   }
   template<> template<>
   bool ArrayDescriptor<0>::operator==(const ArrayDescriptor<0> & rhs) {
      if (dim != rhs.dim) return false;
      for (int i = 0; i < dim; i++) {
         if (lim[i].low != rhs.lim[i].low) return false;
         if (lim[i].high != rhs.lim[i].high) return false;
      }

      return true;
   }

   template<> template<>
   bool ArrayDescriptor<0>::operator!=(const ArrayDescriptor<0> & rhs) {
      return ! operator==(rhs);
   }
}
