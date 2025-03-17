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

#ifndef FIXED63_INCLUDED
#define FIXED63_INCLUDED

#include <stdint.h>

namespace pearlrt {

   /**
   \file

   \brief safe FIXED(63) arithmetic  (for internal use)

   The type FIXED(63) is used to store clock and duration values.
   */

   /**
   Helper class for range sensitive 64 bit integer arithemtic

   This class implements integer operations, which throw exceptions if the
   result would be wrong due to the limited number of bits or other undefined
   operation like divide by zero.

      In case an operation would leave the range of 64 bit,
      an exception will be thrown.
      <ul>
      <li>theArithemeticUnderflow
      <li>theArithmeticOverflow
      <li>theArithemeticDivideByZero
      </ul>

   */
   class Fixed63 {
   private:
      /**

      C datatype for 64 bit values
      */
      typedef uint64_t UFixed63_t;

   public:
      /**
        C++ type for Fixed(63) constants
        \returns is just the data type for the internal storage
      */
      typedef int64_t Fixed63_t;
   private:
      /**
      32 bit integer type

      for internal usage within the class
      */
      typedef int32_t __int32;

      /**
      unsigned 32 bit integer type

      for internal usage within the class
      */

      typedef uint32_t __uint32;
      void regMultiply(const UFixed63_t& a,
                       const UFixed63_t& b, UFixed63_t *ret) ;
      Fixed63_t x;
      static const Fixed63_t MinInt = 0x8000000000000000LL;
      static const Fixed63_t MaxInt = 0x7fffffffffffffffLL;
   public:

      /**
         construct to zero
      */
      Fixed63();

      /**
         construct with given value
         \param y the value of type long long (e.g. 27LL)
      */
      Fixed63(Fixed63_t y);

     /**
      return the internal value as Fixed64_t (long long)

      \returns the value
      */
      Fixed63_t get() const;

      /**
      increment the internal value with the given parameter

      \param rhs the value to be added

      \return reference to this object containing the result

      \throws  ArithmeticUnderflowSignal ArithmeticOverflowSignal

      */
      Fixed63 operator+=(const Fixed63& rhs);

      /**
      add two values as in a + b

      \param rhs the value to be added to the current objects value

      \returns a new object containing the result
      \throws  ArithmeticUnderflowSignal ArithmeticOverflowSignal
      */
      Fixed63 operator+(const Fixed63& rhs) const;

      /**
      reduce the internal value with the given parameter

      \param rhs the value to be subtracted

      \return reference to this object containing the result

      \throws  ArithmeticUnderflowSignal ArithmeticOverflowSignal

      */
      Fixed63 operator-=(const Fixed63& rhs);

      /**
      subtract two values as in a - b

      \param rhs reference to the value to be subtracted from
             the current objects value
      \returns a new object containing the result
      \throws  ArithmeticUnderflowSignal ArithmeticOverflowSignal
      */
      Fixed63 operator-(const Fixed63& rhs) const;

      /**
      arithmetic invertion of the value     just like  -a

      \return a new object containing the negative value of this objekt

      \throws ArithmeticOverflowSignal
      */
      Fixed63 operator-() const;

      /**
      divide the current value

      \param rhs reference to the object beeing the denominator

      \returns reference to the current obeject containing the result

      \throws ArithmeticDivideByZeroSignal
      */
      Fixed63 operator/=(const Fixed63& rhs);

      /**
      divide two value like a / b

      \param rhs reference to the denominator
      \returns new object containing the result
      \throws ArithmeticDivideByZeroSignal
      */
      Fixed63 operator/(const Fixed63& rhs) const;

      /**
      modulo operation

      in PEARL it is called REM (reminder)

       PEARL requires  a == (a // b) * b + a REM b;
       where // denotes the integer division<br>
         10 //  3 *  3 =  9 ==>  10 REM  3 =  1    <br>
        -10 //  3 *  3 = -9 ==> -10 REM  3 = -1     <br>
         10 // -1 * -3 =  9 ==>  10 REM -3 =  1    <br>
        -10 // -3 * -3 = -9 ==> -10 REM -3 = -1    <br>
       result has same sign as first operand               <br>
       this behavior is the same as in C++         <br>

      \param rhs reference to the denominator

      \returns reference to the current object containing the result

      \throws ArithmeticDivideByZeroSignal
      */
      Fixed63 operator%=(const Fixed63& rhs);

      /**
      modulo operation

      in PEARL it is called REM (reminder)

       PEARL requires  a == (a // b) * b + a REM b;
       where // denotes the integer division<br>
         10 //  3 *  3 =  9 ==>  10 REM  3 =  1    <br>
        -10 //  3 *  3 = -9 ==> -10 REM  3 = -1     <br>
         10 // -1 * -3 =  9 ==>  10 REM -3 =  1    <br>
        -10 // -3 * -3 = -9 ==> -10 REM -3 = -1    <br>
       result has same sign as first operand               <br>
       this behavior is the same as in C++         <br>

      \param rhs reference to the denominator

      \returns reference to the current object containing the result

      \throws ArithmeticDivideByZeroSignal
      */
      Fixed63 operator%(const Fixed63& rhs) const;

     /**
      multiplication operation


      \param rhs reference to the second multiplicator

      \returns reference to the current object containing the result

      \throws ArithmeticOverflowSignal
      */
      Fixed63 operator*=(const Fixed63& rhs);

     /**
      multiplication operation

      \param rhs reference to the second multiplicator

      \returns new object containing the result

      \throws ArithmeticOverflowSignal
      */
      Fixed63 operator*(const Fixed63& rhs) const;

      /**
      compare operation

      in order to keep the class small, only one compare operation is available

      \param rhs reference to the seconds parameter

      \returns >0, if this objects value > rhs value <br>
      =0, if both values are equal<br>
      <0, if this objects value < rhs value
      */

      int compare(const Fixed63& rhs) const;

   };
}
#endif
