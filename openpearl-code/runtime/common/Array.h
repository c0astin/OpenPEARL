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

#ifndef ARRAY_H_INCLUDED
#define ARRAY_H_INCLUDED


/**
\file

\brief The array descriptor and access routines

\author R. Mueller

Arrays are treated in PEARL in a special way. They may be passed
anonymously to procedures and the procedure does only know
the number of dimensions. There is no limit for array dimensions.

*/


#include <stdio.h>
#include <stdint.h>

#include "Fixed.h"
#include "BitString.h"
#include "Signals.h"
#include "Log.h"

namespace pearlrt {

// some preprocessor definitions

/**
   wrapper macro to enable the passing of array initializer as one parameter
*/
#define LIMITS(...) __VA_ARGS__


/**
   declare an array with the identifier name the given number of dimensions
   and the initialisier of the limits structure

   The name of the array will be of type *Array, which points to the
   concrete array descriptor. This enable the language system to pass
   diffenent arrays to procedures.
   The access to individual array elements is done in the C++ scope
   by the sum of the array data start and the indexed element offset
   withing the data storage.

   The data storage of the array must be defined by the compiler
   as simple C++ array of the desired type.
*/
#define DCLARRAY(name,dimensions,limits) \
   pearlrt::ArrayDescriptor<dimensions> a_##name = { dimensions, limits }; \
//   pearlrt::Array name((pearlrt::ArrayDescriptor<0>*)&a_##name, data_##name ); 


   /**
     The array descriptor.

     It contains the informations about the concrete array structure

     \tparam DIM number of dimensions of the array
   */
   template <int DIM>
   struct ArrayDescriptor {
      /** number of dimensions starting with 1  */
      int dim;
      /**
         The limits for each dimension.
         For each dimension the lower and upper bound is specified,
         and the number of elements in the sub array.
      */
      struct Limits {
         /** lower bound in this dimension	*/
         int low;

         /** upper bound in this dimension	*/
         int high;
         /** number of elements in all subsequent dimensions  */
         int size;
      } lim[DIM]; /**< limits for all dimensions */

      /**
      calculate the offset of the specified element in the data section

      \param i the first index

      \throws IndexOutOfBoundsSignal if a  current index  value is out
           of the limiting bounds

      \returns index of the specified element in the linearized data section
      */
      size_t offset(Fixed<31> i, ...) {
        size_t result;
        va_list args;
        va_start(args,i);
        result = ((ArrayDescriptor<0>*)this)->_offset(i,args);
        return (result);
      }

      /**
      calculate the offset of the specified element in the data section

      \param i the first index

      \throws IndexOutOfBoundsSignal if a  current index  value is out
           of the limiting bounds

      \returns index of the specified element in the linearized data section
      */
      size_t _offset(Fixed<31> i, va_list args);

      /**
       upper bound of given index

       \param x index number starting with 1
       \throws IndexOutOfBoundsSignal if a  requested index  value is out
           of the specified dimensions
       \returns upper bound if this array index
      */
      Fixed<31> upb(Fixed<31> x);

      /**
       lower bound of given index

       \param x index number starting with 1
       \throws IndexOutOfBoundsSignal if a  requested index  value is out
           of the specified dimensions
       \returns lower bound if this array index
      */
      Fixed<31> lwb(Fixed<31> x);

      template <int D2>
      bool operator==(const ArrayDescriptor<D2> & rhs);
      template <int D2>
      bool operator!=(const ArrayDescriptor<D2> & rhs);
   };

   /**
      The array type itself.

      This type contains only the reference to the array descriptor
      and the array access methods
   */
   template <class C>
   class Array {
   private:
      ArrayDescriptor<0>* descriptor;
      C * data;
      Array() {};

   public:
      /**
         The ctor for an array. This initializes the array descriptor.

         The real number of dimensions is stored in the ArrayDescriptor
         itself. Thats why it is possible to use a generic pointer to
         a 'zero'-dimensional array descriptor

         \param descr the array descriptor, which is assoiated with this array
         \param d pointer to the data area of the array

      */
      Array(ArrayDescriptor<0> * descr, C* d) : descriptor(descr), data(d) {}


      /**
        return pointer to the selected array element
        \param i the index list
        \return a pointer to the array element
      */
      C* getPtr(Fixed<31> i, ...) {
        va_list args;
        va_start(args,i);
        C* result = data+descriptor->_offset(i,args);
        va_end(args);
        return (result);
      }
     
      /**
      retrieve the array descriptor 

      \returns the array descriptor of the array
      */ 
      ArrayDescriptor<0> * getDescriptor() {
          return descriptor;
      }

     /**
     retrieve the pointer to the storage of the array

     \returns the starting address of the array data
     */
     void* getDataPtr() {
         return data;
     }

     /**
     calculate to total number of elements of the array
     \returns the total number of elements of thearray
     */
     size_t getTotalNbrOfElements() {
         return descriptor->lim[0].size*
                (descriptor->lim[0].high - descriptor->lim[0].low + 1);
     }

     template <class D>
     BitString<1> operator==(const Array<D>& rhs) {
         if (data != rhs.data) return BitString<1>(0);
         if (descriptor == rhs.descriptor) return BitString<1>(1);
         if (*descriptor != *(rhs.descriptor)) return BitString<1>(0);
         return BitString<1>(1);
     }

     template <class D>
     BitString<1> operator!=(const Array<D>& rhs) {
         return (operator==(rhs)).bitNot();
     }
   };

}
#endif

