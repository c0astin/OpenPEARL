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

\brief Test routines for array operations

\page Testprograms

\section Array Array Tests
Test routines for the array types.

There are several unit tests using the google test framework.

\cond TREAT_EXAMPLES
*/
#include "gtest.h"

#include "Fixed.h"
#include "Array.h"
#include "compare.h"
#include "Signals.h"
#include "Log.h"

/**
test bounds1
*/
TEST(ArrayTest, bounds1) {
   pearlrt::Fixed<31> data_array1[100]; // arrayData Fixed(0:4,0:19);
   pearlrt::ArrayDescriptor<2> ad_array1 = {2,LIMITS({{0,4,20},{0,19,1}})};
   pearlrt::Array<pearlrt::Fixed<31>> array1((pearlrt::ArrayDescriptor<0>*)&ad_array1, data_array1);

   ASSERT_THROW(array1.getDescriptor()->lwb(pearlrt::Fixed<31>(0)),
                pearlrt::ArrayIndexOutOfBoundsSignal);
   ASSERT_EQ(array1.getDescriptor()->lwb(pearlrt::Fixed<31>(1)).x, 0);
   ASSERT_EQ(array1.getDescriptor()->lwb(pearlrt::Fixed<31>(2)).x, 0);
   ASSERT_THROW(array1.getDescriptor()->lwb(pearlrt::Fixed<31>(3)),
                pearlrt::ArrayIndexOutOfBoundsSignal);

   ASSERT_THROW(array1.getDescriptor()->upb(pearlrt::Fixed<31>(0)),
                pearlrt::ArrayIndexOutOfBoundsSignal);
   ASSERT_EQ(array1.getDescriptor()->upb(pearlrt::Fixed<31>(1)).x, 4);
   ASSERT_EQ(array1.getDescriptor()->upb(pearlrt::Fixed<31>(2)).x, 19);
   ASSERT_THROW(array1.getDescriptor()->upb(pearlrt::Fixed<31>(3)),
                pearlrt::ArrayIndexOutOfBoundsSignal);

}


TEST(ArrayTest, bounds) {
   pearlrt::Fixed<31> data_array1[100]; // arrayData Fixed(-4:4,-10:9,20);
   pearlrt::ArrayDescriptor<2> ad_array1 = {2,LIMITS({{-4,4,20},{-10,9,1}})};
   pearlrt::Array<pearlrt::Fixed<31>> array1((pearlrt::ArrayDescriptor<0>*)&ad_array1, data_array1);


   ASSERT_THROW(array1.getDescriptor()->lwb(pearlrt::Fixed<31>(0)),
                pearlrt::ArrayIndexOutOfBoundsSignal);
   ASSERT_EQ(array1.getDescriptor()->lwb(pearlrt::Fixed<31>(1)).x, -4);
   ASSERT_EQ(array1.getDescriptor()->lwb(pearlrt::Fixed<31>(2)).x, -10);
   ASSERT_THROW(array1.getDescriptor()->lwb(pearlrt::Fixed<31>(3)),
                pearlrt::ArrayIndexOutOfBoundsSignal);

   ASSERT_THROW(array1.getDescriptor()->upb(pearlrt::Fixed<31>(0)),
                pearlrt::ArrayIndexOutOfBoundsSignal);
   ASSERT_EQ(array1.getDescriptor()->upb(pearlrt::Fixed<31>(1)).x, 4);
   ASSERT_EQ(array1.getDescriptor()->upb(pearlrt::Fixed<31>(2)).x, 9);
   ASSERT_THROW(array1.getDescriptor()->upb(pearlrt::Fixed<31>(3)),
                pearlrt::ArrayIndexOutOfBoundsSignal);
}

TEST(ArrayTest, readwrite) {
   pearlrt::Fixed<31> testvalue;

   pearlrt::Fixed<31> data_array1[100]; // arrayData Fixed(0:4,0:19);
   pearlrt::ArrayDescriptor<2> ad_array1 = {2,LIMITS({{0,4,20},{0,19,1}})};
   pearlrt::Array<pearlrt::Fixed<31>> array1((pearlrt::ArrayDescriptor<0>*)&ad_array1, data_array1);


   // read, and index exception tests
   ASSERT_NO_THROW(
      testvalue=*(array1.getPtr(pearlrt::Fixed<31>(0),pearlrt::Fixed<31>(0)))
    );
   ASSERT_NO_THROW(
      testvalue=*(array1.getPtr(pearlrt::Fixed<31>(4),pearlrt::Fixed<31>(19)))
    );
   ASSERT_THROW(
      testvalue=*(array1.getPtr(pearlrt::Fixed<31>(5),pearlrt::Fixed<31>(19))),
                pearlrt::ArrayIndexOutOfBoundsSignal);
   ASSERT_THROW(
      testvalue=*(array1.getPtr(pearlrt::Fixed<31>(4),pearlrt::Fixed<31>(20))),
                pearlrt::ArrayIndexOutOfBoundsSignal);

   ASSERT_THROW(
      testvalue=*(array1.getPtr(pearlrt::Fixed<31>(0),pearlrt::Fixed<31>(-1))),
                pearlrt::ArrayIndexOutOfBoundsSignal);
   ASSERT_THROW(
      testvalue=*(array1.getPtr(pearlrt::Fixed<31>(-1),pearlrt::Fixed<31>(0))),
                pearlrt::ArrayIndexOutOfBoundsSignal);

   // write
   ASSERT_NO_THROW(
      testvalue=*(array1.getPtr(pearlrt::Fixed<31>(1),pearlrt::Fixed<31>(0)));
      *(array1.getPtr(pearlrt::Fixed<31>(0),pearlrt::Fixed<31>(1))) = testvalue;
    );
}


TEST(ArrayTest, order) {
   pearlrt::Fixed<31> testvalue;
   pearlrt::Fixed<31> data_array1[100]; // arrayData Fixed(5,20);
   pearlrt::ArrayDescriptor<2> ad_array1 = {2,LIMITS({{0,4,20},{0,19,1}})};
   pearlrt::Array<pearlrt::Fixed<31>> array1((pearlrt::ArrayDescriptor<0>*)&ad_array1, data_array1);


   // test pointer differences
   // last index runs first
   ASSERT_EQ(
      (array1.getPtr(pearlrt::Fixed<31>(0),pearlrt::Fixed<31>(1)))-
      (array1.getPtr(pearlrt::Fixed<31>(0),pearlrt::Fixed<31>(0))), 1);
   ASSERT_EQ(
      (array1.getPtr(pearlrt::Fixed<31>(4),pearlrt::Fixed<31>(0)))-
      (array1.getPtr(pearlrt::Fixed<31>(0),pearlrt::Fixed<31>(0))), 80);
   ASSERT_EQ(
      (array1.getPtr(pearlrt::Fixed<31>(4),pearlrt::Fixed<31>(19)))-
      (array1.getPtr(pearlrt::Fixed<31>(0),pearlrt::Fixed<31>(0))), 99);

}

void preset(pearlrt::Array<pearlrt::Fixed<31>> * a) {
   for (int i=a->getDescriptor()->lwb(pearlrt::Fixed<31>(1)).x;
             i<=a->getDescriptor()->upb(pearlrt::Fixed<31>(1)).x;i++) {
   for (int j=a->getDescriptor()->lwb(pearlrt::Fixed<31>(2)).x;
             j<=a->getDescriptor()->upb(pearlrt::Fixed<31>(2)).x;j++) {
      *(a->getPtr(pearlrt::Fixed<31>(i),pearlrt::Fixed<31>(j)))=
       pearlrt::Fixed<31>(100*i+j);
   }}
}

void dump(pearlrt::Array<pearlrt::Fixed<31>> * a) {
   // run fast running index in inner loop
   for (int i=a->getDescriptor()->lwb(pearlrt::Fixed<31>(1)).x;
             i<=a->getDescriptor()->upb(pearlrt::Fixed<31>(1)).x;i++) {
   for (int j=a->getDescriptor()->lwb(pearlrt::Fixed<31>(2)).x;
             j<=a->getDescriptor()->upb(pearlrt::Fixed<31>(2)).x;j++) {
      printf("%04d ", (int)(
        a->getPtr(pearlrt::Fixed<31>(i),pearlrt::Fixed<31>(j)))->get());
   }
   printf("\n");
   }
}


TEST(ArrayTest, functionpass) {
   pearlrt::Fixed<31> data_array1[100]; // arrayData Fixed(10,10);
   pearlrt::ArrayDescriptor<2> ad_array1 = {2,LIMITS({{0,9,10},{0,9,1}})};
   pearlrt::Array<pearlrt::Fixed<31>> array1((pearlrt::ArrayDescriptor<0>*)&ad_array1, data_array1);

   preset(&array1);
   dump(&array1);
}

TEST(ArrayTest,withStructs){
   /* DCL s(0:20) STRUCT [ d FIXED(31), data(0:9,0:9) FIXED(31) , d2 FIXED(31)];
      DCL testValue FIXED(31);
      testValue = s(2).d;
      testValue = s(2).data(0,1);
   */
   struct S {
      int d;
      pearlrt::Fixed<31> data_fixedArray[100]; // arrayData Fixed(0:9,0:9);
      int d2;
   } data_structs[20];
   pearlrt::ArrayDescriptor<1> ad_struct = {1,LIMITS({{0,20,1}})};
   pearlrt::Array<struct S> arrayStructs((pearlrt::ArrayDescriptor<0>*)&ad_struct, data_structs);
   pearlrt::ArrayDescriptor<2> ad_fixedArray = {2,LIMITS({{0,9,10},{0,9,1}})};
   
   pearlrt::Fixed<31> testValue;
   //  testValue = s(2).d;
   testValue = arrayStructs.getPtr(pearlrt::Fixed<31>(2))->d;

  testValue =*( arrayStructs.getPtr(pearlrt::Fixed<31>(2))->data_fixedArray+
        ad_fixedArray.offset(pearlrt::Fixed<31>(0), pearlrt::Fixed<31>(1)));

#if 0
   // testValue = s(2).data(0,1);
   {
       struct S * sel = arrayStructs.getPtr(pearlrt::Fixed<31>(2));
       pearlrt::Array<pearlrt::Fixed<31>> fixedArray((pearlrt::ArrayDescriptor<0>*)&ad_fixedArray,sel->data_fixedArray);
       testValue = *(fixedArray.getPtr(pearlrt::Fixed<31>(0),pearlrt::Fixed<31>(1)));
   }
#endif
}

TEST(ArrayTest,Compare) {
   pearlrt::Fixed<31> testvalue;
   pearlrt::Fixed<31> data_array1[100]; // arrayData Fixed(0:4,20);
   pearlrt::ArrayDescriptor<2> ad_array1 = {2,LIMITS({{0,4,20},{0,19,1}})};
   pearlrt::Array<pearlrt::Fixed<31>> array1((pearlrt::ArrayDescriptor<0>*)&ad_array1, data_array1);

   pearlrt::ArrayDescriptor<2> ad_array1a = {2,LIMITS({{0,4,20},{0,19,1}})};
   pearlrt::Array<pearlrt::Fixed<31>> array1a((pearlrt::ArrayDescriptor<0>*)&ad_array1a, data_array1);
   pearlrt::Array<pearlrt::Fixed<31>> array1same((pearlrt::ArrayDescriptor<0>*)&ad_array1, data_array1);

   pearlrt::Fixed<31> data_array2[100]; // arrayData Fixed(5,20);
   pearlrt::ArrayDescriptor<2> ad_array2 = {2,LIMITS({{1,5,20},{0,19,1}})};
   pearlrt::Array<pearlrt::Fixed<31>> array2((pearlrt::ArrayDescriptor<0>*)&ad_array2, data_array2);

   ASSERT_TRUE( (array1==array1a).getBoolean()); // diferent AD with same values
   ASSERT_TRUE( (array1==array1same).getBoolean());
   ASSERT_FALSE( (array1==array2).getBoolean());

}
/**
\endcond
*/
