/*
 [The "BSD license"]
 Copyright (c) 2012-2013 Rainer Mueller
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


\brief safe FIXED(63) operations   (for internal use)

*/

#include "Fixed63.h"
#include "Signals.h"
#include <stdio.h>
#include <cinttypes>  // formats for int64_t, etc

namespace pearlrt {


   Fixed63::Fixed63() {
      x = 0;
   }

   Fixed63::Fixed63(Fixed63_t y) {
      x = y;
   }


   Fixed63::Fixed63_t Fixed63::get() const {
      return x;
   }

   Fixed63 Fixed63::operator+=(const Fixed63& rhs) {
      if (!((x ^ rhs.x) < 0)) {                    //test for +/- combo
         //either two negatives or two positives
         if (rhs.x < 0) {
            //two negatives
            if (x < MinInt - rhs.x) {              //remember rhs < 0
               throw theArithmeticUnderflowSignal;
            }

            //ok
         } else {
            //two positives
            if (MaxInt - x < rhs.x) {
               throw theArithmeticOverflowSignal;
            }

            //OK
         }
      }

      //else overflow not possible
      x += rhs.x;
      return *this;
   }

   Fixed63 Fixed63::operator+(const Fixed63& rhs) const {
      return Fixed63(*this) += rhs;
   }

   Fixed63 Fixed63::operator-=(const Fixed63& rhs) {
      // Note: lhs is *this
      // we have essentially 4 cases:
      //
      // 1) lhs positive, rhs positive - overflow not possible
      // 2) lhs positive, rhs negative - equivalent to addition
      //                                 - result >= lhs or error
      // 3) lhs negative, rhs positive - check result <= lhs
      // 4) lhs negative, rhs negative - overflow not possible
      UFixed63_t ux = *(UFixed63_t*)&x;
      UFixed63_t ur = *(UFixed63_t*)&rhs.x;
      ux = ux - ur; 
      Fixed63_t tmp = *(Fixed63_t*)(&ux);


      // Note - ideally, we can order these so that true conditionals
      // lead to success, which enables better pipelining
      // It isn't practical here
      if (x >= 0 && rhs.x < 0 && tmp < x) {   // condition 2
         throw theArithmeticOverflowSignal;
      }

      if (rhs.x >= 0 && tmp > x) {             // condition 3
         throw theArithmeticUnderflowSignal;
      }

      x = tmp;
      return *this;
   }


   Fixed63 Fixed63::operator-(const Fixed63& rhs) const {
      return Fixed63(*this) -= rhs;
   }

   Fixed63 Fixed63::operator-() const {
      if (x == MinInt) {
         throw theArithmeticOverflowSignal;
      }

      return Fixed63(-x);
   }


   Fixed63 Fixed63::operator/=(const Fixed63& rhs) {
      if (rhs.x == 0) {
         throw theArithmeticDivideByZeroSignal;
      }

      if (x == MinInt && rhs.x == -1) {
         throw theArithmeticOverflowSignal;
      }

      x /= rhs.x;
      return *this;
   }


   Fixed63 Fixed63::operator/(const Fixed63& rhs) const {
      return Fixed63(*this) /= rhs;
   }


   Fixed63 Fixed63::operator%=(const Fixed63& rhs) {
      if (rhs.x == 0) {
         throw theArithmeticDivideByZeroSignal;
      }

      x = (rhs.x == -1) ? 0 : x % rhs.x;
      return (*this);
   }


   Fixed63 Fixed63::operator%(const Fixed63& rhs) const {
      return Fixed63(*this) %= rhs;
   }


   /**
   internal 64 bit multiplication with exception throwing
   in case of range overflow
   */
   void Fixed63::regMultiply(const UFixed63_t& a,
                             const UFixed63_t& b, UFixed63_t *ret) {
      __uint32 aHigh, aLow, bHigh, bLow;
//printf("Fixed63::regMultiply called sizeof(uFixed63)=%d\n", sizeof(UFixed63_t));
//printf("   a=%" PRIx64 " b=%" PRIx64 "\n", a,b);

      // Consider that a*b can be broken up into:
      // (aHigh * 2^32 + aLow) * (bHigh * 2^32 + bLow)
      // => (aHigh * bHigh * 2^64) +
      //          (aLow * bHigh * 2^32) + (aHigh * bLow * 2^32) +
      //          (aLow * bLow)
      // Note - same approach applies for 128 bit math on a 64-bit system

      aHigh = (__uint32)(a >> 32);
      aLow  = (__uint32)a;
      bHigh = (__uint32)(b >> 32);
      bLow  = (__uint32)b;
//printf("aHigh=%08x aLow=%08x bHigh=%08x bLow=%08x\n", aHigh,aLow,bHigh,bLow);

      *ret = 0;

      if (aHigh == 0) {
         if (bHigh != 0) {
            *ret = (UFixed63_t)aLow * (UFixed63_t)bHigh;
         }
      } else if (bHigh == 0) {
         if (aHigh != 0) {
            *ret = (UFixed63_t)aHigh * (UFixed63_t)bLow;
         }
      } else {
         // both operands have an non 0 high part --> this crashes
//printf("Fixed63::mult:@1 throw ArithmetricOverflowSignal\n");
         throw theArithmeticOverflowSignal;
      }

      if (*ret != 0) {
         UFixed63_t tmp;

         if ((__uint32)(*ret >> 32) != 0) {
            // the previous result is larger than 32 bit but
            // we need to multiply with 2^32 --> this will crash
            throw theArithmeticOverflowSignal;
         }

         // multiply with 2^32
         *ret <<= 32;
        
         // the add can not crash, since all lower 32 bits are zero 
         tmp = (UFixed63_t)aLow * (UFixed63_t)bLow;
         *ret += tmp;

         if (*ret < tmp) {
            // if this addition results in a smaller value 
            // we had an overflow! 
//printf("Fixed63::mult:@2 throw ArithmetricOverflowSignal\n");
            throw theArithmeticOverflowSignal;
         }

         return;
      }

      // this is the easy part --- both parameters are
      // below 2^32 --- no overflow possible
      *ret = (UFixed63_t)aLow * (UFixed63_t)bLow;
   }

   Fixed63 Fixed63::operator*=(const Fixed63& rhs) {
      UFixed63_t tmp;
      Fixed63_t y = rhs.x;
      bool lhsNegative = false;
      bool rhsNegative = false;

//printf("*= called:   this=%" PRIx64 " rhs=%" PRIx64 " y=%" PRIx64 "\n", x,rhs.x ,y);
//printf("             this=%" PRId64 " rhs=%" PRId64 " y=%" PRId64 "\n", x,rhs.x ,y);
      if (x < 0) {
         lhsNegative = true;

         if (x == MinInt && y != 1) {
//printf("Fixed63::*=:@1 throw ArithmetricOverflowSignal\n");
            throw theArithmeticOverflowSignal;
         }

         x = -x;
//printf(" x= -x; -->    this=%" PRId64 "\n", x);
      }

      if (y < 0) {
         rhsNegative = true;

         if (y == MinInt && x != 1) {
//printf("Fixed63::*=:@2 throw ArithmetricOverflowSignal\n");
            throw theArithmeticOverflowSignal;
         }

         y = -y;
//printf(" y= -y; -->    y=%" PRId64 "\n", y);
      }

//printf("call regMultiply   ()x=%" PRIx64 " ()y=%" PRIx64 "\n", 
//                  (unsigned Fixed63_t)x, *(UFixed63_t*)&y);
//printf("             this=%" PRIx64 " rhs=%" PRIx64 " y=%" PRIx64 "\n", x,rhs.x ,y);
      UFixed63_t ux = *(UFixed63_t*)&x;
      UFixed63_t uy = *(UFixed63_t*)&y;
//printf("             ux=%" PRIx64 " uy=%" PRIx64 "\n", ux,uy);

      regMultiply(ux, uy, &tmp);
      //regMultiply(*(unsigned Fixed63_t*)&x, *(unsigned Fixed63_t*)&y, &tmp);
      x = (Fixed63_t)tmp;

      if (x < 0) {
//printf("Fixed63::*=:@3 throw ArithmetricOverflowSignal\n");
         throw theArithmeticOverflowSignal;
      }

      if (lhsNegative ^ rhsNegative) {
         x = -x;
      }

      return (*this);
   }



   Fixed63 Fixed63::operator*(const Fixed63& rhs) const {
      return Fixed63(*this) *= rhs;
   }

   int Fixed63::compare(const Fixed63& rhs) const {
      if (x < rhs.x) {
         return -1;
      } else if (x > rhs.x) {
         return +1;
      }

      return 0;
   }

}
