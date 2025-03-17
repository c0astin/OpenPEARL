#include <stdlib.h>
#include <cstdarg>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <pthread.h>
#include <semaphore.h>
#include <string>
#include <cmath>
#include <math.h>
#include <inttypes.h>
#include <stddef.h>
#include <unistd.h>
#include <termios.h>
#include <fcntl.h>
#include <signal.h>
#include <cstdint>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <vector>
#include <iostream>
#include <set>
#include <map>
#include <algorithm>
#include <cstring>
#include <stack>
#include <sstream>
#include <chrono>
#include <mutex>


#define REF_H_INCLUDED

#define SIGNALS_INCLUDES

namespace pearlrt {

   class Signal {
   private:
      static Signal ** signalVector;
      static size_t nbrOfSignals;
   public:

      static void throwSignalByRst(int rst);
   protected:
      char* type;
      int rstNum;
      int currentRst;
   public:

      Signal();

      const char* which(void);

      int whichRST(void);

      void induce(void);

      void induce(int r);

      int getCurrentRst(void);
   };


class InternalSignal : public Signal {
 public:
 InternalSignal() {
   type = (char*)"internal signal";
   rstNum = 2000;
 };
};
class InternalTaskSignal : public InternalSignal {
 public:
 InternalTaskSignal() {
   type = (char*)"internal tasking signal";
   rstNum = 2001;
 };
};
class InternalDationSignal : public InternalSignal {
 public:
 InternalDationSignal() {
   type = (char*)"problem in dation";
   rstNum = 2002;
 };
};
class InternalDatatypeSignal : public InternalSignal {
 public:
 InternalDatatypeSignal() {
   type = (char*)"Illegal operation in expression";
   rstNum = 2003;
 };
};
class InternalConfigurationSignal : public InternalSignal {
 public:
 InternalConfigurationSignal() {
   type = (char*)"Configuration error";
   rstNum = 2005;
 };
};
class TerminateRequestSignal : public InternalSignal {
 public:
 TerminateRequestSignal() {
   type = (char*)"TerminateRequest";
   rstNum = 2006;
 };
};
class ArithmeticOverflowSignal : public InternalSignal {
 public:
 ArithmeticOverflowSignal() {
   type = (char*)"arithmetic overflow";
   rstNum = 2007;
 };
};
class ArithmeticUnderflowSignal : public InternalSignal {
 public:
 ArithmeticUnderflowSignal() {
   type = (char*)"arithmetic underflow";
   rstNum = 2008;
 };
};
class ArithmeticDivideByZeroSignal : public InternalSignal {
 public:
 ArithmeticDivideByZeroSignal() {
   type = (char*)"arithmetic divide by zero";
   rstNum = 2009;
 };
};
class InternalSignalsSignal : public InternalSignal {
 public:
 InternalSignalsSignal() {
   type = (char*)"problem in signal administration";
   rstNum = 2010;
 };
};
class AnySignal : public Signal {
 public:
 AnySignal() {
   type = (char*)"*** will never be printed ***";
   rstNum = 1000;
 };
};
class TaskSignal : public Signal {
 public:
 TaskSignal() {
   type = (char*)"*** will never be printed ***";
   rstNum = 1100;
 };
};
class PrioOutOfRangeSignal : public TaskSignal {
 public:
 PrioOutOfRangeSignal() {
   type = (char*)"priority out of range";
   rstNum = 1101;
 };
};
class TaskRunningSignal : public TaskSignal {
 public:
 TaskRunningSignal() {
   type = (char*)"activate an active task";
   rstNum = 1102;
 };
};
class TaskSuspendedSignal : public TaskSignal {
 public:
 TaskSuspendedSignal() {
   type = (char*)"Illegal operation on suspended task";
   rstNum = 1103;
 };
};
class TaskTerminatedSignal : public TaskSignal {
 public:
 TaskTerminatedSignal() {
   type = (char*)"Illegal operation on terminated task";
   rstNum = 1104;
 };
};
class IllegalSchedulingSignal : public TaskSignal {
 public:
 IllegalSchedulingSignal() {
   type = (char*)"illegal scheduling condition";
   rstNum = 1105;
 };
};
class SemaOverflowSignal : public TaskSignal {
 public:
 SemaOverflowSignal() {
   type = (char*)"semaphore value overflow";
   rstNum = 1110;
 };
};
class BoltStateSignal : public TaskSignal {
 public:
 BoltStateSignal() {
   type = (char*)"wrong bolt state";
   rstNum = 1111;
 };
};
class BoltReserveDuplicateSignal : public TaskSignal {
 public:
 BoltReserveDuplicateSignal() {
   type = (char*)"duplicate bolt in RESERVE list";
   rstNum = 1112;
 };
};
class DataValueSignal : public Signal {
 public:
 DataValueSignal() {
   type = (char*)"illegal value ";
   rstNum = 1200;
 };
};
class FixedRangeSignal : public DataValueSignal {
 public:
 FixedRangeSignal() {
   type = (char*)"fixed overflow";
   rstNum = 1201;
 };
};
class FixedDivideByZeroSignal : public DataValueSignal {
 public:
 FixedDivideByZeroSignal() {
   type = (char*)"Fixed divide by zero";
   rstNum = 1202;
 };
};
class DurationDivideByZeroSignal : public DataValueSignal {
 public:
 DurationDivideByZeroSignal() {
   type = (char*)"duration div 0";
   rstNum = 1203;
 };
};
class DurationRangeSignal : public DataValueSignal {
 public:
 DurationRangeSignal() {
   type = (char*)"duration value overflow";
   rstNum = 1204;
 };
};
class FloatIsNaNSignal : public DataValueSignal {
 public:
 FloatIsNaNSignal() {
   type = (char*)"float result is NaN";
   rstNum = 1205;
 };
};
class FloatIsINFSignal : public DataValueSignal {
 public:
 FloatIsINFSignal() {
   type = (char*)"float result is INF";
   rstNum = 1206;
 };
};
class FunctionParameterOutOfRangeException : public DataValueSignal {
 public:
 FunctionParameterOutOfRangeException() {
   type = (char*)"parameter for arithmetic operator out of range";
   rstNum = 1207;
 };
};
class BitIndexOutOfRangeSignal : public DataValueSignal {
 public:
 BitIndexOutOfRangeSignal() {
   type = (char*)"bit index out of range";
   rstNum = 1208;
 };
};
class CharacterTooLongSignal : public DataValueSignal {
 public:
 CharacterTooLongSignal() {
   type = (char*)"character string too long";
   rstNum = 1209;
 };
};
class CharacterIndexOutOfRangeSignal : public DataValueSignal {
 public:
 CharacterIndexOutOfRangeSignal() {
   type = (char*)"character index out of range";
   rstNum = 1210;
 };
};
class CharIsINVSignal : public DataValueSignal {
 public:
 CharIsINVSignal() {
   type = (char*)"write of INV CHAR";
   rstNum = 1211;
 };
};
class ArrayIndexOutOfBoundsSignal : public DataValueSignal {
 public:
 ArrayIndexOutOfBoundsSignal() {
   type = (char*)"array Index out of Bounds";
   rstNum = 1212;
 };
};
class RefNotInitializedSignal : public DataValueSignal {
 public:
 RefNotInitializedSignal() {
   type = (char*)"reference not initialized";
   rstNum = 1213;
 };
};
class BadArraySegmentSignal : public DataValueSignal {
 public:
 BadArraySegmentSignal() {
   type = (char*)"array slice must select at least 1 element";
   rstNum = 1214;
 };
};
class DationSignal : public Signal {
 public:
 DationSignal() {
   type = (char*)"*** will never be printed ***";
   rstNum = 1300;
 };
};
class DationParamSignal : public DationSignal {
 public:
 DationParamSignal() {
   type = (char*)"illegal parameter, not permitted operation";
   rstNum = 1302;
 };
};
class DationNotOpenSignal : public DationSignal {
 public:
 DationNotOpenSignal() {
   type = (char*)"Dation not opened";
   rstNum = 1303;
 };
};
class OpenFailedSignal : public DationSignal {
 public:
 OpenFailedSignal() {
   type = (char*)"device doesn't exist or no permission";
   rstNum = 1304;
 };
};
class CloseFailedSignal : public DationSignal {
 public:
 CloseFailedSignal() {
   type = (char*)"invalid file descriptor or i/o error";
   rstNum = 1305;
 };
};
class PositioningFailedSignal : public DationSignal {
 public:
 PositioningFailedSignal() {
   type = (char*)"Positioning the Dations failed";
   rstNum = 1306;
 };
};
class InvalidPositioningSignal : public DationSignal {
 public:
 InvalidPositioningSignal() {
   type = (char*)"Positioning forbidden, because wrong or no dimension";
   rstNum = 1307;
 };
};
class ReadingFailedSignal : public DationSignal {
 public:
 ReadingFailedSignal() {
   type = (char*)"reading data from dation failed";
   rstNum = 1308;
 };
};
class WritingFailedSignal : public DationSignal {
 public:
 WritingFailedSignal() {
   type = (char*)"writing data to dation failed";
   rstNum = 1309;
 };
};
class DationIndexBoundSignal : public DationSignal {
 public:
 DationIndexBoundSignal() {
   type = (char*)"Dation position out of bounds";
   rstNum = 1310;
 };
};
class DationNotSupportedSignal : public DationSignal {
 public:
 DationNotSupportedSignal() {
   type = (char*)"Dation operation not supported";
   rstNum = 1311;
 };
};
class DationEOFSignal : public DationSignal {
 public:
 DationEOFSignal() {
   type = (char*)"EOF encountered";
   rstNum = 1312;
 };
};
class NoEOFDationSignal : public DationSignal {
 public:
 NoEOFDationSignal() {
   type = (char*)"EOF not supported";
   rstNum = 1313;
 };
};
class DationTFURecordSignal : public DationSignal {
 public:
 DationTFURecordSignal() {
   type = (char*)"record is longer than TFU size";
   rstNum = 1314;
 };
};
class DationDatatypeSignal : public DationSignal {
 public:
 DationDatatypeSignal() {
   type = (char*)"data type does not match with format element";
   rstNum = 1315;
 };
};
class DationFormatRepetitionValue : public DationSignal {
 public:
 DationFormatRepetitionValue() {
   type = (char*)"number of repetitions must be >=0";
   rstNum = 1316;
 };
};
class DationFormatRepetitionOverflow : public DationSignal {
 public:
 DationFormatRepetitionOverflow() {
   type = (char*)"too many loops in format";
   rstNum = 1317;
 };
};
class DeviceNotFoundSignal : public DationSignal {
 public:
 DeviceNotFoundSignal() {
   type = (char*)"Device not found";
   rstNum = 1301;
 };
};
class FormatSignal : public Signal {
 public:
 FormatSignal() {
   type = (char*)"format";
   rstNum = 1400;
 };
};
class NoMoreCharactersSignal : public FormatSignal {
 public:
 NoMoreCharactersSignal() {
   type = (char*)"no more characters found";
   rstNum = 1401;
 };
};
class DurationFormatSignal : public FormatSignal {
 public:
 DurationFormatSignal() {
   type = (char*)"illegal parameter for D-format";
   rstNum = 1402;
 };
};
class ClockFormatSignal : public FormatSignal {
 public:
 ClockFormatSignal() {
   type = (char*)"illegal parameter for T-format";
   rstNum = 1403;
 };
};
class FixedFormatSignal : public FormatSignal {
 public:
 FixedFormatSignal() {
   type = (char*)"illegal parameter for F-format";
   rstNum = 1404;
 };
};
class CharacterFormatSignal : public FormatSignal {
 public:
 CharacterFormatSignal() {
   type = (char*)"illegal parameter for A-format";
   rstNum = 1405;
 };
};
class BitFormatSignal : public FormatSignal {
 public:
 BitFormatSignal() {
   type = (char*)"illegal parameter for B-format";
   rstNum = 1406;
 };
};
class ClockValueSignal : public FormatSignal {
 public:
 ClockValueSignal() {
   type = (char*)"illegal value for T-format";
   rstNum = 1407;
 };
};
class FixedValueSignal : public FormatSignal {
 public:
 FixedValueSignal() {
   type = (char*)"Illegal value for F-format ";
   rstNum = 1408;
 };
};
class BitValueSignal : public FormatSignal {
 public:
 BitValueSignal() {
   type = (char*)"Illegal value for B-format ";
   rstNum = 1409;
 };
};
class DurationValueSignal : public FormatSignal {
 public:
 DurationValueSignal() {
   type = (char*)"Illegal value for D-format ";
   rstNum = 1410;
 };
};
class ExpFormatSignal : public FormatSignal {
 public:
 ExpFormatSignal() {
   type = (char*)"illegal parameter for E-format";
   rstNum = 1411;
 };
};
class ExpValueSignal : public FormatSignal {
 public:
 ExpValueSignal() {
   type = (char*)"Illegal value for E-format ";
   rstNum = 1412;
 };
};
extern InternalSignal theInternalSignal;
extern InternalTaskSignal theInternalTaskSignal;
extern InternalDationSignal theInternalDationSignal;
extern InternalDatatypeSignal theInternalDatatypeSignal;
extern InternalConfigurationSignal theInternalConfigurationSignal;
extern TerminateRequestSignal theTerminateRequestSignal;
extern ArithmeticOverflowSignal theArithmeticOverflowSignal;
extern ArithmeticUnderflowSignal theArithmeticUnderflowSignal;
extern ArithmeticDivideByZeroSignal theArithmeticDivideByZeroSignal;
extern InternalSignalsSignal theInternalSignalsSignal;
extern AnySignal theAnySignal;
extern TaskSignal theTaskSignal;
extern PrioOutOfRangeSignal thePrioOutOfRangeSignal;
extern TaskRunningSignal theTaskRunningSignal;
extern TaskSuspendedSignal theTaskSuspendedSignal;
extern TaskTerminatedSignal theTaskTerminatedSignal;
extern IllegalSchedulingSignal theIllegalSchedulingSignal;
extern SemaOverflowSignal theSemaOverflowSignal;
extern BoltStateSignal theBoltStateSignal;
extern BoltReserveDuplicateSignal theBoltReserveDuplicateSignal;
extern DataValueSignal theDataValueSignal;
extern FixedRangeSignal theFixedRangeSignal;
extern FixedDivideByZeroSignal theFixedDivideByZeroSignal;
extern DurationDivideByZeroSignal theDurationDivideByZeroSignal;
extern DurationRangeSignal theDurationRangeSignal;
extern FloatIsNaNSignal theFloatIsNaNSignal;
extern FloatIsINFSignal theFloatIsINFSignal;
extern FunctionParameterOutOfRangeException theFunctionParameterOutOfRangeException;
extern BitIndexOutOfRangeSignal theBitIndexOutOfRangeSignal;
extern CharacterTooLongSignal theCharacterTooLongSignal;
extern CharacterIndexOutOfRangeSignal theCharacterIndexOutOfRangeSignal;
extern CharIsINVSignal theCharIsINVSignal;
extern ArrayIndexOutOfBoundsSignal theArrayIndexOutOfBoundsSignal;
extern RefNotInitializedSignal theRefNotInitializedSignal;
extern BadArraySegmentSignal theBadArraySegmentSignal;
extern DationSignal theDationSignal;
extern DationParamSignal theDationParamSignal;
extern DationNotOpenSignal theDationNotOpenSignal;
extern OpenFailedSignal theOpenFailedSignal;
extern CloseFailedSignal theCloseFailedSignal;
extern PositioningFailedSignal thePositioningFailedSignal;
extern InvalidPositioningSignal theInvalidPositioningSignal;
extern ReadingFailedSignal theReadingFailedSignal;
extern WritingFailedSignal theWritingFailedSignal;
extern DationIndexBoundSignal theDationIndexBoundSignal;
extern DationNotSupportedSignal theDationNotSupportedSignal;
extern DationEOFSignal theDationEOFSignal;
extern NoEOFDationSignal theNoEOFDationSignal;
extern DationTFURecordSignal theDationTFURecordSignal;
extern DationDatatypeSignal theDationDatatypeSignal;
extern DationFormatRepetitionValue theDationFormatRepetitionValue;
extern DationFormatRepetitionOverflow theDationFormatRepetitionOverflow;
extern DeviceNotFoundSignal theDeviceNotFoundSignal;
extern FormatSignal theFormatSignal;
extern NoMoreCharactersSignal theNoMoreCharactersSignal;
extern DurationFormatSignal theDurationFormatSignal;
extern ClockFormatSignal theClockFormatSignal;
extern FixedFormatSignal theFixedFormatSignal;
extern CharacterFormatSignal theCharacterFormatSignal;
extern BitFormatSignal theBitFormatSignal;
extern ClockValueSignal theClockValueSignal;
extern FixedValueSignal theFixedValueSignal;
extern BitValueSignal theBitValueSignal;
extern DurationValueSignal theDurationValueSignal;
extern ExpFormatSignal theExpFormatSignal;
extern ExpValueSignal theExpValueSignal;
}

#define LOG_INCLUDED


#define SYSTEMDATIONNB_INCLUDED

#define SYSTEMDATION_INCLUDED

#define DATION_INCLUDED

#define DEVICE_INCLUDED
namespace pearlrt {

   class Device {
   };
}

#define FIXED_H_INCLUDED




#define FIXED63_INCLUDED

namespace pearlrt {


   class Fixed63 {
   private:

      typedef uint64_t UFixed63_t;
   public:

      typedef int64_t Fixed63_t;
   private:

      typedef int32_t __int32;

      typedef uint32_t __uint32;
      void regMultiply(const UFixed63_t& a,
                       const UFixed63_t& b, UFixed63_t *ret) ;
      Fixed63_t x;
      static const Fixed63_t MinInt = 0x8000000000000000LL;
      static const Fixed63_t MaxInt = 0x7fffffffffffffffLL;
   public:

      Fixed63();

      Fixed63(Fixed63_t y);

      Fixed63_t get() const;

      Fixed63 operator+=(const Fixed63& rhs);

      Fixed63 operator+(const Fixed63& rhs) const;

      Fixed63 operator-=(const Fixed63& rhs);

      Fixed63 operator-(const Fixed63& rhs) const;

      Fixed63 operator-() const;

      Fixed63 operator/=(const Fixed63& rhs);

      Fixed63 operator/(const Fixed63& rhs) const;

      Fixed63 operator%=(const Fixed63& rhs);

      Fixed63 operator%(const Fixed63& rhs) const;

      Fixed63 operator*=(const Fixed63& rhs);

      Fixed63 operator*(const Fixed63& rhs) const;

      int compare(const Fixed63& rhs) const;
   };
}

#define AUTOCONF_H_INCLUDED
#define CONFIG_SIMFILEXTRUDER 1
#define CONFIG_LINUX_PriorityMapper_MaxPrio 49
#define CONFIG_INSTALL_Target "/usr/local"
#define CONFIG_LINUX 1
#define CONFIG_SIMWATERTANK 1
#define CONFIG_COMPILER_ANTLR "/usr/local/lib/antlr-4.8-complete.jar"

/**
\file stackcheck.h
*/
#ifndef NOSTACKCHECK
#if (TARGET==2 && CONFIG_LPC1768_CHECK_STACK_OVERFLOW==1)
/**
Stack checking is useful but it costs lot of execution time for
templated functions.
There is an option to enable/disbale stack checking on the
microcontroller part of the OpenPEARL system.
On normal linux systems this option is not supported by gcc.
*/
#define NOSTACKCHECK __attribute__((no_instrument_function))
#else
/**
\def NOSTACKCHECK
Stack checking is useful but it costs lot of execution time for
templated functions.
There is an option to enable/disbale stack checking on the
microcontroller part of the OpenPEARL system.
On normal linux systems this option is not supoorted by gcc.
*/
#define NOSTACKCHECK /* nothing */
#endif
#endif

#define IFTHENELSETEMPLATE_INCLUDED
namespace pearlrt {

   template< bool Condition, class THEN, class ELSE > struct IF { };

   template<class THEN, class ELSE>
   struct IF<true, THEN, ELSE > {

      typedef THEN SELECT_CLASS;
   };

   template<class THEN, class ELSE >
   struct IF<false, THEN, ELSE > {

      typedef ELSE SELECT_CLASS;
   };
}

#define NUMBEROFBYTES_INCLUDE
namespace pearlrt {

   template< unsigned int len > class NumberOfBytes {
   public:

      enum NumberOfBytesLen {
         NumberOfBytesN0 = (0 < len && len <= 8) ? 1 : 0,
         NumberOfBytesN1 = (8 < len && len <= 16) ? 2 : 0,
         NumberOfBytesN2 = (16 < len && len <= 32) ? 4 : 0,
         NumberOfBytesN3 = (32 < len && len <= 64) ? 8 : 0,
      };

      enum NumberOfBytesNbr {resultFixed = NumberOfBytesN0 + NumberOfBytesN1 +
                              NumberOfBytesN2 + NumberOfBytesN3};

      enum NumberOfBytesB {
         NumberOfBytesB0 = (0 < len && len <= 8) ? 1 : 0,
         NumberOfBytesB1 = (8 < len && len <= 16) ? 2 : 0,
         NumberOfBytesB2 = (16 < len && len <= 32) ? 4 : 0,
         NumberOfBytesB3 = (32 < len && len <= 64) ? 8 : 0,
      };

      enum R {resultBitString = NumberOfBytesB0 + NumberOfBytesB1 +
                                NumberOfBytesB2 + NumberOfBytesB3};
   };
}
namespace pearlrt {
   template<int S> class FixedTypes;

   template<> class FixedTypes<1> {
   public:

      typedef int8_t NativeFixedType;

      typedef uint8_t UNativeFixedType;

      typedef int16_t LongerNativeFixedType;
   };

   template<> class FixedTypes<2> {
   public:

      typedef int16_t NativeFixedType;

      typedef uint16_t UNativeFixedType;

      typedef int32_t LongerNativeFixedType;
   };

   template<> class FixedTypes<4> {
   public:

      typedef int32_t NativeFixedType;

      typedef uint32_t UNativeFixedType;

      typedef int64_t LongerNativeFixedType;
   };

   template<> class FixedTypes<8> {
   public:

      typedef int64_t NativeFixedType;

      typedef uint64_t UNativeFixedType;

      typedef int64_t LongerNativeFixedType;
   };


   template<int S> class Fixed {
   public:
   private:

      static const int len = NumberOfBytes < S + 1 >::resultFixed;
   public:

      typedef typename FixedTypes<len>::NativeFixedType NativeType;

      typedef typename FixedTypes<len>::UNativeFixedType UNativeType;

      typedef typename FixedTypes<len>::LongerNativeFixedType LongerNativeType;

      NativeType x;

      NativeType get() const NOSTACKCHECK {
         return x;
      }
   private:
      struct LessThan32Bits {
         static Fixed add(const Fixed & lhs,
                          const Fixed & rhs) NOSTACKCHECK  {
            Fixed h;
            LongerNativeType r;
            h.x = rhs.x + lhs.x;
            r = rhs.x;
            r += lhs.x;
            if (h.x != r || r < minValue || r > maxValue) {
               throw theFixedRangeSignal;
            }
            return h;
         }
         static Fixed complement(const Fixed & lhs)  {
            Fixed h;
            LongerNativeType r;
            h.x = -lhs.x;
            r = 0;
            r -= lhs.x;
            if (h.x != r || r < minValue || r > maxValue) {
               throw theFixedRangeSignal;
            }
            return h;
         }
         static Fixed substract(const Fixed & lhs,
                                const Fixed & rhs) {
            Fixed h;
            LongerNativeType r;
            h.x = lhs.x - rhs.x;
            r = lhs.x;
            r -= rhs.x;
            if (r != h.x || r < minValue || r > maxValue) {
               throw theFixedRangeSignal;
            }
            return h;
         }
         static Fixed multiply(const Fixed & lhs,
                               const Fixed & rhs) {
            Fixed h;
            LongerNativeType r;
            r = rhs.x;
            r *= lhs.x;
            if (r < minValue || r > maxValue) {
               throw theFixedRangeSignal;
            }
            h = r;
            return h;
         }
         static Fixed divide(const Fixed & lhs,
                             const Fixed & rhs) {
            Fixed h;
            LongerNativeType r;
            if (rhs.x == 0) {
               throw theFixedDivideByZeroSignal;
            }
            r = lhs.x;
            r /= rhs.x;
            if (r < minValue || r > maxValue) {
               throw theFixedRangeSignal;
            }
            h.x = r;
            return h;
         }
         static Fixed modulo(const Fixed & lhs, const Fixed & rhs) {
            Fixed h;
            if (rhs.x == 0) {
               throw theFixedDivideByZeroSignal;
            }
            if (lhs.x == minValue && rhs.x == -1) {
               return 0;
            }
            h.x = lhs.x % rhs.x;
            return h;
         }
      };
      struct MoreThan31Bits {
         static Fixed add(const Fixed & lhs,
                          const Fixed & rhs)  {
            Fixed r;
            Fixed63 h(lhs.x);
            h += Fixed63(rhs.x);
            r.x = h.get();
            if (r.x < minValue || r.x > maxValue) {
               throw theFixedRangeSignal;
            }
            return r;
         }
         static Fixed complement(const Fixed& lhs) {
            Fixed r;
            Fixed63 h;
            h =  -Fixed63(lhs.x);
            r.x = h.get();
            if (r.x < minValue || r.x > maxValue) {
               throw theFixedRangeSignal;
            }
            return r;
         }
         static Fixed substract(const Fixed& lhs, const Fixed & rhs) {
            Fixed r;
            Fixed63 h(lhs.x);
            h -= Fixed63(rhs.x);
            r.x = h.get();
            if (r.x < minValue || r.x > maxValue) {
               throw theFixedRangeSignal;
            }
            return r;
         }
         static Fixed multiply(const Fixed& lhs, const Fixed& rhs) {
            Fixed r;
            Fixed63 h(lhs.x);
            h *= Fixed63(rhs.x);
            r.x = h.get();
            if (r.x < minValue || r.x > maxValue) {
               throw theFixedRangeSignal;
            }
            return r;
         }
         static Fixed divide(const Fixed& lhs, const Fixed& rhs) {
            Fixed r;
            Fixed63 h(lhs.x);
            if (rhs.x == 0) {
               throw theFixedDivideByZeroSignal;
            }
            h /= Fixed63(rhs.x);
            r.x = h.get();
            if (r.x < minValue || r.x > maxValue) {
               throw theFixedRangeSignal;
            }
            return r;
         }
         static Fixed modulo(const Fixed& lhs, const Fixed& rhs) {
            Fixed r;
            Fixed63 h(lhs.x);
            h %= Fixed63(rhs.x);
            r.x = h.get();
            if (r.x < minValue || r.x > maxValue) {
               throw theFixedRangeSignal;
            }
            return r;
         }
      };
   private:
      static const NativeType maxValue =
         (((UNativeType)(-1))) >> (sizeof(x) * 8 - S);
      static const NativeType minValue =
         (((UNativeType)(-1)) >> S) << S;
      static const LongerNativeType mask = ((LongerNativeType)minValue) << 1;
   public:

      Fixed() NOSTACKCHECK {
         x = 0;
      }

      Fixed(LongerNativeType y) NOSTACKCHECK {
         if (y > maxValue || y < minValue) {
            throw theFixedRangeSignal;
         }
         x = y;
      }

      template<int P> Fixed operator=(const Fixed<P> & y) {
         if (y.x > maxValue || y.x < minValue) {
            throw theFixedRangeSignal;
         }
         x = y.x;
         return *this;
      }

      template<int P> operator Fixed<P> () const {
         Fixed<P> result(x);
         return result;
      }

      Fixed abs() {
         Fixed y(x);
         if (x < 0) {
            y = Fixed(-x);
         }
         return y;
      }

      Fixed<1> sign() {
         if (x < 0) {
            return Fixed<1>(-1);
         } else if (x > 0) {
            return Fixed<1>(1);
         }
         return Fixed<1>(0);
      }

      Fixed add(const Fixed & rhs) const {
         return (IF < (S < 32),
                 LessThan32Bits,
                 MoreThan31Bits >::SELECT_CLASS::add(*this, rhs)
                );
      }

      Fixed operator- () const {
         return (IF < (S < 32),
                 LessThan32Bits,
                 MoreThan31Bits >::SELECT_CLASS::complement(*this)
                );
      }

      Fixed substract(const Fixed & rhs) const {
         return (IF < (S < 32),
                 LessThan32Bits,
                 MoreThan31Bits >::
                 SELECT_CLASS::substract(*this, rhs)
                );
      }

      Fixed multiply(const Fixed & rhs) const {
         return (IF < (S < 32),
                 LessThan32Bits,
                 MoreThan31Bits >::
                 SELECT_CLASS::multiply(*this, rhs)
                );
      }

      Fixed divide(const Fixed & rhs) const {
         return (IF < (S < 32),
                 LessThan32Bits,
                 MoreThan31Bits >::
                 SELECT_CLASS::divide(*this, rhs)
                );
      }

      Fixed modulo(const Fixed & rhs) const {
         return (IF < (S < 32),
                 LessThan32Bits,
                 MoreThan31Bits >::
                 SELECT_CLASS::modulo(*this, rhs)
                );
      }

      template<int P>Fixed pow(const Fixed<P>& rhs) const {
         Fixed result(1);
         Fixed base(x);
         int64_t exp = rhs.x;
         if (exp == 0) {
            result.x = 1;
         } else  if (exp > 0) {
            try {
               while (exp) {
                  if (exp & 1) {
                     result = result * base;
                  }
                  exp >>= 1;
                  if (exp) {
                     base = base * base;
                  }
               }
            } catch (ArithmeticOverflowSignal e) {
               throw  theFixedRangeSignal;
            }
            if (result.x > maxValue || result.x < minValue) {
               throw  theFixedRangeSignal;
            }
         } else if (exp < 0) {
            if (x != 0) {
               result.x = 0;
            } else {
              throw theFixedDivideByZeroSignal;
            }
         }
         return result;
      }

      template<int P>
      Fixed<P> fit(const Fixed<P>& rhs) {
         Fixed<P> result;
         result = *this;
         return result;
      }
   };


   template<int S, int P>
   Fixed < (S > P) ? S : P > operator+ (Fixed<S> lhs, const Fixed<P> & rhs) {
      Fixed < (S > P) ? S : P > l(lhs.x);
      Fixed < (S > P) ? S : P > r(rhs.x);
      return l.add(r);
   }

   template<int S, int P>
   Fixed < (S > P) ? S : P > operator- (Fixed<S> lhs, const Fixed<P> & rhs) {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return l.substract(r);
   }

   template<int S, int P>
   Fixed < (S > P) ? S : P > operator* (Fixed<S> lhs, const Fixed<P> & rhs) {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return l.multiply(r);
   }

   template<int S, int P>
   Fixed < (S > P) ? S : P > operator/ (Fixed<S> lhs, const Fixed<P> & rhs) {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return l.divide(r);
   }

   template<int S, int P>
   Fixed < (S > P) ? S : P > operator% (Fixed<S> lhs, const Fixed<P> & rhs) {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return l.modulo(r);
   }

   void assignInt32ToFixedViaVoidPointer(
      void* voidPointerToFixed, size_t lengthOfFixed, int32_t valueToAssign);
}
# undef NOSTACKCHECK
namespace pearlrt {
   class TaskCommon;


   class Dation: public Device {
   public:

      enum DationParams {

         IN = 0x0001,

         OUT = 0x0002,

         INOUT = 0x0004,

         IDF = 0x0008,

         ANY = 0x0010,

         NEW = 0x0020,

         OLD = 0x0040,

         PRM = 0x0080,

         CAN = 0x0100,

         DIRECT = 0x0200,

         FORWARD = 0x0400,

         FORBACK = 0x0800,

         RST = 0x1000,

         CYCLIC = 0x2000,

         NOCYCL = 0x4000,

         STREAM = 0x08000,

         NOSTREAM = 0x10000,

         OPENPRM = 0x20000,

         OPENCAN = 0x40000,

         OPENOLD = 0x80000,

         OPENNEW = 0x100000,

         OPENANY = 0x200000,

         OPENIDF = 0x400000,

         OPENNOIDF = 0x800000,

         CLOSEPRM = 0x1000000,

         CLOSECAN = 0x2000000
      };

      static const int DIRECTIONMASK = IN | OUT | INOUT;

      static const int OPENMASK = IDF | ANY | OLD | NEW |
                                  OPENIDF | OPENNOIDF | OPENOLD | OPENNEW | OPENANY |
     CAN | PRM |
    OPENCAN | OPENPRM ;

      static const int CLOSEMASK = PRM | CAN | CLOSEPRM | CLOSECAN;

      static const int POSITIONINGMASK = DIRECT | FORWARD | FORBACK;

      static const int DIMMASK = STREAM | CYCLIC | NOSTREAM | NOCYCL;

      enum DationStatus {

         OPENED = 1,

         CLOSED = 2
      };

      int dationParams;

      DationStatus dationStatus;

      virtual void dationRead(void * destination, size_t size) = 0;

      virtual void dationWrite(void * destination, size_t size) = 0;

      virtual void suspend(TaskCommon * ioPerformingTask) = 0;

      virtual void terminate(TaskCommon * ioPerformingTask) = 0;
   };

}

#define REFCHAR_INCLUDED



#define CHARACTER_INCLUDED



namespace pearlrt {

   int characterCompare(const char * s1, int l1, const char* s2, int l2);

   void characterFillSpaces(char * dest, int len);

   void characterSafeCopy(char*dest, const char* source, int len) ;

   template<size_t length>
   class Character {
   public:

      char data[length];
   public:

      Character<length>() {
         size_t i;
         if (length > 32767 || length < 1) {
            printf("Character: illegal length (%d)\n", (int)length);
            throw theCharacterTooLongSignal;
         }
         for (i = 0; i < length; i++) {
            data[i] = ' ';
         }
      }

      Character(const char * string) {
         unsigned int i;
         unsigned int l = strlen(string);
         if (length > 32767 || l > length) {
            printf("Character: illegal length (%d)\n", (int)length);
            throw theCharacterTooLongSignal;
         }
         for (i = 0; i < l; i++) {
            data[i] = string[i];
         }
         for (i = l; i < length; i++) {
            data[i] = ' ';
         }
      }

      Character(int len, const char * string) {
         unsigned int i;
         unsigned int l = len;
         if (length > 32767 || l > length) {

            throw theCharacterTooLongSignal;
         }
         for (i = 0; i < l; i++) {
            data[i] = string[i];
         }
         for (i = l; i < length; i++) {
            data[i] = ' ';
         }
      }

      template<size_t T> Character(Character<T> string) {

         if (length < T) {

            throw theCharacterTooLongSignal;
         }
         characterSafeCopy(data, string.data, T);
         characterFillSpaces(data + T, length - T);
      }

      Fixed<15> upb() const {
         return Fixed<15>(length);
      }

      Character<1>  getCharAt(Fixed<15> p) {
         if ((size_t)p.x > length || p.x < 1) {

            throw theCharacterIndexOutOfRangeSignal;
         }
         Character<1> c;
         c.data[0] = data[p.x - 1];
         return c;
      }

      void setCharAt(Fixed<15> p, Character<1> c) {
         if ((size_t)p.x > length || p.x < 1) {

            throw theCharacterIndexOutOfRangeSignal;
         }
         data[p.x - 1] = c.data[0];
      }

      Fixed<7> toFixed() {
         if (length > 1) {

            throw theCharacterTooLongSignal;
         }
         Fixed<7> result;
         result.x = data[0];
         return result;
      }

      char *  get() {
         return data;
      }

      template<size_t LRHS>
      Character<length>&  operator=(const Character<LRHS> & rhs) {
         size_t i;
         if (length < LRHS) {
            printf("internal error: assign to smaller string\n");
            throw theInternalSignal;
         }


         if ((void*)this != (void*)&rhs) {
            characterSafeCopy(data, rhs.data, LRHS);
            characterFillSpaces(data + LRHS, length - LRHS);
         }
         return *this;
      }

      template<size_t LRHS>
      Character < length + LRHS > catChar(Character<LRHS> & rhs) {
         Character < length + LRHS > result;
         characterSafeCopy(result.data, data, length);
         characterSafeCopy(result.data + length, rhs.data, LRHS);
         return result;
      }
   };




   template <int T>
   Character<1>  toChar(Fixed<T> x) {
       if (x.x >= -128 && x.x <= 127) {
           Character<1> c;
           c.data[0] = x.x;
           return c;
       }
       throw theFixedRangeSignal;
   }
}

namespace pearlrt {



   class RefCharacter {
   private:
      bool charIsINV;
   public:
      size_t max;
      size_t current;
      char * data;


      RefCharacter();

      template<size_t S>
      RefCharacter(Character<S> & rhs) {
         max = rhs.upb().x;
         current = max;
         data = &rhs.data[0];
         charIsINV = false;
      }

      template<size_t S>
      RefCharacter(const Character<S> & rhs) {
         max = rhs.upb().x;
         current = max;
         data = (char*) &rhs.data[0];
         charIsINV = true;
      }

      template<size_t S>
      void setWork(const Character<S> & rhs) {
         max = rhs.upb().x;
         current = max;





         data = (char*)&rhs.data[0];

      }

      RefCharacter(char * s);
      void dump() const;

      void setWork(void * s, size_t len);

      bool equal(const RefCharacter* const rhs) const;

      char getCharAt(size_t index) const;

      char getNextChar();

      char * getCstring() ;

      void rewind();

      void fill();

      void add(const RefCharacter & rhs);

      void add(const char * rhs);

      Fixed<31> getMax();

      Fixed<31> getCurrent() const;

      char* getDataPtr() const;

      void setCurrent(size_t newpos);

      void add(const char rhs);

      template<size_t S>
      void add(const Character<S> & rhs) {
         if ((size_t)(rhs.upb().x) > (max - current)) {
            throw theCharacterTooLongSignal;
         }
         for (size_t i = 0; i < (size_t)(rhs.upb().x); i++) {
            data[current + i] = rhs.data[i];
         }
         current += rhs.upb().x;
      }

      template<size_t S>
      void store(Character<S> & dest) {
         size_t len;
         len = current;
         if (len > (size_t)(dest.upb().x)) {
            throw theCharacterTooLongSignal;
         }
         for (size_t i = 0; i < len; i++) {
            dest.data[i] = data[i];
         }
         if (len < (size_t)(dest.upb().x)) {
            for (size_t i = len; i < (size_t)(dest.upb().x); i++) {
               dest.data[i] = ' ';
            }
         }
      }
   };

}

namespace pearlrt {


   class SystemDation: public Dation {
   public:

      virtual int capabilities() = 0;

      virtual void dationClose(int closeParam) = 0;

      virtual bool allowMultipleIORequests();

      virtual void registerWaitingTask(void * task, int direction);

      virtual void triggerWaitingTask(void * task, int direction);

      virtual void suspend(TaskCommon * ioPerformingTask);

      void terminate(TaskCommon * ioPerformingTask);
   };
}


namespace pearlrt {


   class SystemDationNB: public SystemDation {
   public:

      virtual SystemDationNB* dationOpen(const RefCharacter * idfValue,
                                         int openParam) = 0;

      virtual void dationUnGetChar(const char c) = 0;

      virtual void translateNewLine(bool doNewLineTranslation) = 0;

      virtual void dationSeek(const Fixed<31> & p, const int dationParam) ;

      virtual Fixed<31> dationEof();

      virtual void informIOOperationCompleted(Dation::DationParams dir);
   };
}


namespace pearlrt {
#define ERRORMESSAGE "\n                     **** above line truncated ****\n"

   class Log {
   private:
      static int logFileHandle;
      static bool initialized;
      static Log* instance;
      static bool ctorIsActive;
      bool definedInSystemPart;

      static int logLevel;
      static SystemDationNB * provider;

      void doit(const Character<7>& type, const char * format,
                       va_list args);
   public:

      static void doFormat(const Character<7>& type,
                           RefCharacter &rc,
                           const char * format,
                           va_list args);

      enum LogLevel {DEBUG = 1, INFO = 2, WARN = 4, ERROR = 8, LINETRACE=16};

      static void setLevel(int level);

      Log();

      Log(SystemDationNB * _provider, char* level);

      static Log* getInstance();

      bool isDefinedInSystemPart();

      void setDefinedInSystemPart(bool fromSystemPart);

      static void info(const char * format, ...)
      __attribute__((format(printf, 1, 2)));

      static void error(const char * format, ...)
      __attribute__((format(printf, 1, 2)));

      static void lineTrace(const char * format, ...)
      __attribute__((format(printf, 1, 2)));

      static void warn(const char * format, ...)
      __attribute__((format(printf, 1, 2)));

      static void debug(const char * format, ...)
      __attribute__((format(printf, 1, 2)));

      static void exit();
   };
}
namespace pearlrt {


   template<class C> class Ref {
   public:
      C * x;
   public:

      Ref() {
         x = NULL;
      }

      Ref(C& pValue) : x(&pValue) {}








      C& operator*() {
         if (x) {
            return *x;
         }
         Log::error("Ref::use of uninitialized reference");
         throw theRefNotInitializedSignal;
      }

      C& operator->() {
         if (x) {
            return *x;
         }
         Log::error("Ref::use of uninitialized reference");
         throw theRefNotInitializedSignal;
      }



      void operator=( C &rhs) {
        x = &rhs;
      }

      void operator=( Ref<char> &rhs) {
        x = (C*)0;
      }

      C* get() {
        return x;
      }
   };

}

#define TASK_INCLUDED



#define BITSTRING_H_INCLUDED
template<int S> class BitString;







/**
\file stackcheck.h
*/
#ifndef NOSTACKCHECK
#if (TARGET==2 && CONFIG_LPC1768_CHECK_STACK_OVERFLOW==1)
/**
Stack checking is useful but it costs lot of execution time for
templated functions.
There is an option to enable/disbale stack checking on the
microcontroller part of the OpenPEARL system.
On normal linux systems this option is not supported by gcc.
*/
#define NOSTACKCHECK __attribute__((no_instrument_function))
#else
/**
\def NOSTACKCHECK
Stack checking is useful but it costs lot of execution time for
templated functions.
There is an option to enable/disbale stack checking on the
microcontroller part of the OpenPEARL system.
On normal linux systems this option is not supoorted by gcc.
*/
#define NOSTACKCHECK /* nothing */
#endif
#endif
namespace pearlrt {



   template<int S> class Bits;

   template<> class Bits<1> {
   public:

      typedef uint8_t BitType;

      typedef int8_t SignedBitType;
   };

   template<> class Bits<2> {
   public:

      typedef uint16_t BitType;

      typedef int16_t  SignedBitType;
   };

   template<> class Bits<4> {
   public:

      typedef uint32_t BitType;

      typedef int32_t  SignedBitType;
   };

   template<> class Bits<8> {
   public:

      typedef uint64_t BitType;

      typedef int64_t  SignedBitType;
   };


   template<int S> class BitString {
   private:

      static const int len = NumberOfBytes<S>::resultBitString;
   public:

      typedef typename Bits<len>::BitType DataType;

      typedef typename Bits<len>::SignedBitType SignedDataType;

      DataType x;
   private:
      static const int shiftSize = sizeof(x) * 8 - S;
      static const DataType mask = ((DataType)(-1) >> shiftSize)
                                   << shiftSize;
   public:

      BitString() NOSTACKCHECK {
         x = 0;
      }

      BitString(DataType y) NOSTACKCHECK {
         x = y;
         x <<= shiftSize;
         x &= mask;
      }

      template <int F>
      BitString(Fixed < F > y) NOSTACKCHECK {

         if (S == 64) {
            Log::error("Fixed 64 not supported");
            throw theInternalSignal;
         }
           if (y.x < 0) {
                Log::error("TOBIT: sign would be lost");
                throw theFixedRangeSignal;
            }


            x = y.x;
            x <<= shiftSize;
            x &= mask;
      }

      template <int P> BitString(BitString<P> y) {
         *this = y;
      }

      BitString<S> bitShift(const Fixed<15> l) {
         BitString<S> retval;
         retval.x = x;
         if (l.x > 0) {
            retval.x <<= l.x;
         } else {
            retval.x >>= -l.x;
         }
         retval.x &= mask;
         return (retval);
      }

      BitString<S> bitCshift(const Fixed<15> l) {
         BitString<S> retval;
         retval.x = x;
         Fixed<15> effectiveShift;



         effectiveShift = l.modulo(S);
         if (effectiveShift.x > 0) {
            retval.x <<= effectiveShift.x;
            retval.x |= (x >> (S - effectiveShift.x));
         } else {
            retval.x >>= -effectiveShift.x;
            retval.x |= x << (S + effectiveShift.x);
         }
         retval.x &= mask;
         return (retval);
      }

      BitString<S> bitNot() {
         BitString z;
         z.x = ~ x;
         z.x &= mask;
         return (z);
      }

      template<int P> BitString < S + P >
      bitCat(BitString<P> y) {
         BitString < S + P > z, z1;

         z.x = x << ((sizeof(z) - sizeof(x)) * 8);

         if (sizeof(z) > sizeof(y)) {
            z1.x = y.x << ((sizeof(z) - sizeof(y)) * 8 - S);
         } else {
            z1.x = y.x >> S;
         }

         z.x |= z1.x;
         return z;
      }

      template <int P> BitString<P>
      getSlice(const Fixed<15>start, BitString<P>* dummy) {
         if (start.x - 1 + P > S || start.x < 1) {
            Log::error(".BIT(%d:%d) index out of range (1:%d)",start.x,start.x+P-1,S);
            throw theBitIndexOutOfRangeSignal;
         }
         DataType s;
         s = x >> (sizeof(s) * 8 - (start.x - 1 + P));
         BitString<P> returnValue(s);
         return returnValue;
      }

      template <int P> void
      setSlice(const Fixed<15>start, const Fixed<15> end,
               const BitString<P> slice) {
         if (start.x - 1 + P > S || start.x < 1 ||
               end.x - start.x + 1 < P) {
            Log::error("index out of range BIT(%d).setBit(%d:%d) := BIT(%d)",
 S,start.x,start.x+P-1,P);
            throw theBitIndexOutOfRangeSignal;
         }
         DataType s, m;
         static const int lengthAdjust = (sizeof(s) - sizeof(slice)) * 8;


         s = slice.x;
         s <<= lengthAdjust;
         s >>= start.x - 1;







         m = mask;
         m <<= start.x - 1;
         m >>= start.x - 1;
         m >>= (sizeof(m) * 8 - end.x);
         m <<= (sizeof(m) * 8 - end.x);
         x &= ~m;
         x |= s;
         return;
      }

      BitString<1> getBit(const Fixed<15>start) const {
         if (start.x > S || start.x < 1) {
            Log::error("index out of range BIT(1:%d).BIT(%d)",S, start.x);
            throw theBitIndexOutOfRangeSignal;
         }
         DataType s;
         s = x >> (sizeof(s) * 8 - start.x);
         BitString<1> returnValue(s);
         return returnValue;
      }

      void setBit(const Fixed<15>start, const BitString<1>newValue) {
         if (start.x > S || start.x < 1) {
            Log::error(".BIT(%d) index out of range (1:%d)",start.x,S);
            throw theBitIndexOutOfRangeSignal;
         }
         DataType s;
         s = 1 << (sizeof(s) * 8 - start.x);
         if (newValue.x) {
            x |= s;
         } else {
            x &= ~s;
         }
         return;
      }

      Fixed < S > toFixed() const {
         Fixed < S > returnValue;
         returnValue.x = ((SignedDataType)x) >> shiftSize;
         return returnValue;
      }

      bool inline getBoolean() const {
         return (!!x);
      }
   private:







      template<int P> struct THENAND {
         static BitString < (S < P) ? P : S >
         bitAnd(BitString<S>x, BitString<P> y) {
            BitString < (S < P) ? P : S > z;
            z.x = x.x;
            z.x <<= (sizeof(y) - sizeof(x)) * 8;
            z.x &= y.x;
            return z;
         }
         static BitString < (S < P) ? P : S >
         bitOr(BitString<S> x, BitString<P> y) {
            BitString < (S < P) ? P : S > z;
            z.x = x.x;
            z.x <<= (sizeof(y) - sizeof(x)) * 8;
            z.x |= y.x;
            return z;
         }
         static BitString < (S < P) ? P : S >
         bitXor(BitString<S> x, BitString<P> y) {
            BitString < (S < P) ? P : S > z;
            z.x = x.x;
            z.x <<= (sizeof(y) - sizeof(x)) * 8;
            z.x ^= y.x;
            return z;
         }
         static void
         bitAssign(BitString<S>* x, BitString<P> y) {
            Log::error("assignment only allowed to larger or equal length "
                       " Bit(%d) := Bit(%d)", S, P);
            throw theInternalDatatypeSignal;
         }
         static BitString<1>
         bitCompare(const BitString<S> x, const BitString<P> y) {
            BitString < (S < P) ? P : S > z;
            z.x = x.x;
            z.x <<= (sizeof(y) - sizeof(x)) * 8;
            if (z.x == y.x) {
               BitString<1> result(1);
               return result;
            } else {
               BitString<1> result(0);
               return result;
            }
         }
      };

      template<int P> struct ELSEAND {
         static BitString < (S < P) ? P : S >
         bitAnd(BitString<S> x, BitString<P> y) {
            BitString < (S < P) ? P : S > z;
            z.x = y.x;
            z.x <<= (sizeof(x) - sizeof(y)) * 8;
            z.x &= x.x;
            return z;
         }
         static BitString < (S < P) ? P : S >
         bitOr(BitString<S> x, BitString<P> y) {
            BitString < (S < P) ? P : S > z;
            z.x = y.x;
            z.x <<= (sizeof(x) - sizeof(y)) * 8;
            z.x |= x.x;
            return z;
         }
         static BitString < (S < P) ? P : S >
         bitXor(BitString<S> x, BitString<P> y) {
            BitString < (S < P) ? P : S > z;
            z.x = y.x;
            z.x <<= (sizeof(x) - sizeof(y)) * 8;
            z.x ^= x.x;
            return z;
         }
         static void
         bitAssign(BitString<S>* x, BitString<P> y) {
            x->x = y.x;
            x->x <<= (sizeof(*x) - sizeof(y)) * 8;
            return;
         }
         static BitString<1>
         bitCompare(const BitString<S> x, const BitString<P> y) {
            BitString < (S < P) ? P : S > z;
            z.x = y.x;
            z.x <<= (sizeof(x) - sizeof(y)) * 8;
            if (z.x == x.x) {
               BitString<1> result(1);
               return result;
            } else {
               BitString<1> result(0);
               return result;
            }
         }
      };
   public:

      template< int P>
      BitString <S> operator= (const BitString<P> y) {
         IF < (S < P), THENAND<P>, ELSEAND<P> >::
         SELECT_CLASS::bitAssign(this, y);
         return *this;
      }

      template<int P>
      BitString < (S < P) ? P : S > bitAnd(BitString<P> y) {
         return IF < (S < P), THENAND<P>, ELSEAND<P> >::
                SELECT_CLASS::bitAnd(*this, y);
      }

      template<int P>
      BitString < (S < P) ? P : S > bitOr(BitString<P> y) {
         return IF < (S < P), THENAND<P>, ELSEAND<P> >::
                SELECT_CLASS::bitOr(*this, y);
      }

      template< int P>
      BitString < (S < P) ? P : S > bitXor(BitString<P> y) {
         return IF < (S < P), THENAND<P>, ELSEAND<P> >::
                SELECT_CLASS::bitXor(*this, y);
      }

      template<int P>
      BitString<1> operator== (const BitString<P> y) const {
         return IF < (S < P), THENAND<P>, ELSEAND<P> >::
                SELECT_CLASS::bitCompare(*this, y);
      }

      template<int P>
      BitString<1> operator!= (const BitString<P> y) const {
         return (IF < (S < P), THENAND<P>, ELSEAND<P> >::
                 SELECT_CLASS::bitCompare(*this, y)).bitNot();
      }
   };

}
# undef NOSTACKCHECK

#define PRIOMAPPER_INCLUDED

namespace pearlrt {


   class PrioMapper {
   private:
      PrioMapper();

      int max;
      static PrioMapper* instance;

      uint8_t mappedPearlPrios[255];
      uint8_t previousMapping[255];
      int usedPearlPrios;
      int usedRtPrios;
      int minUsedPearlPrio;
      int maxUsedPearlPrio;

      bool emitErrorAtFirstTimeBinning;
      void resetUsedPearlPrios(int reserveRequestedPrio);
      void applyBinning();
      void dump();
   public:

      static PrioMapper* getInstance();

      int fromPearl(const Fixed<15> p);

      int getSystemPrio();

      void logPriorities();
   };

}

#define PRIO_INCLUDED
namespace pearlrt {


   class Prio {
   private:
      Fixed<15> prio;
   public:

      Prio();

      Prio(const Fixed<15>& p);

      Fixed<15> get() const;
   };
}

#define CSEMA_INCLUDED


#define CSEMACOMMON_INCLUDED
namespace pearlrt {


   class CSemaCommon {
   protected:

      const char * id;
   public:

      CSemaCommon();

      void name(const char * s);

      virtual void request() = 0;

      virtual void release() = 0;
   };
}
namespace pearlrt {


   class CSema : public CSemaCommon {
   private:
      sem_t sem;
      const char * id;
   public:

      CSema(int preset = 0);

      void name(const char * s);

      ~CSema();

      void request();

      void release();
   };

}

#define CLOCK_H_INCLUDED



namespace pearlrt {
   class Clock;
}

#define DURATION_H_INCLUDED


#define FLOAT_H_INCLUDED


#define FLOATHELPER_H_INCLUDED


namespace pearlrt {

   class FloatHelper {
   public:

      static void testFloatResult(double x);
   };
}


#ifndef NAN
#error "Float.h needs IEEE754 support"
#endif
namespace pearlrt {

   template < int T > struct InternalFloatType;

   template < > struct InternalFloatType<23> {

      typedef float InternalType;
   };

   template < > struct InternalFloatType<52> {

      typedef double InternalType;
   };


   template<int S> class Float {
   public:

      template<int fixedSize, int floatSize> struct FloatResult;

      template<int fixedSize > struct FloatResult<fixedSize, 23> {
         typedef Float < fixedSize <= 23 ? 23 : 52 > ResultType;
      };

      template<int fixedSize> struct FloatResult<fixedSize, 52> {

         typedef  Float<52>  ResultType;
      };

      typedef typename InternalFloatType<S>::InternalType InternalType1;

      InternalType1 x;

      Float<S>() {
         x = NAN;
      }

      Float<S>(double xx) {
         x = xx;
         FloatHelper::testFloatResult(x);
      }

      template <int T>
      Float<S>(Fixed<T> xx) {
         x = xx.x;
      }

      template <int T>
      Float<S>(Float<T> xx) {
         x = xx.x;
      }

      template<int T> Float < S < T ? 0 : S > operator=(const Float<T> &rhs) {
         x = rhs.x;
         FloatHelper::testFloatResult(x);
         return *this;
      }

      template<int T>Float < S < T ? T : S >
      operator+(const Float<T> & rhs) const {
         Float < S < T ? T : S > result;
         result.x = x + rhs.x;
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      template<int T, template<int> class R>
      typename FloatResult<T, S>::ResultType
      operator+(const R<T> & rhs) const {
         typename FloatResult<T, S>::ResultType result;
         result.x = x + rhs.x;
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Float<S> operator-() const {
         Float<S> result;
         result.x = -x;
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      template<int T, template<int> class R>
      typename FloatResult<T, S>::ResultType
      operator-(const R<T> & rhs) const {
         typename FloatResult<T, S>::ResultType result;
         result.x = x - rhs.x;
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      template<int T, template<int> class R>
      typename FloatResult<T, S>::ResultType
      operator*(const R<T> & rhs) const {
         typename FloatResult<T, S>::ResultType result;
         result.x = x * rhs.x;
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      template<int T, template<int> class R>
      typename FloatResult<T, S>::ResultType
      operator/(const R<T> & rhs) const {
         typename FloatResult<T, S>::ResultType result;
         result.x = x / rhs.x;
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      template<int T> Float<T> fit(const Float<T> &rhs) const {
         Float<T> result;
         result.x = x;
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Float<S> abs() const {
         Float<S> result;
         result.x = std::fabs(x);
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Fixed<1> sign() const {
         Fixed<1> result(0);
         if (x < 0) {
            result.x = -1;
         } else if (x > 0) {
            result.x = 1;
         }
         return result;
      }

      Fixed<S> entier() const {
         Fixed<S> result;
         if (x >= 0) {
            result.x = x;
         } else {
            result.x = -x;
            if (result.x != -x) {
               result = result + Fixed<S>(1);
            }
            result = -result;
         }
         return result;
      }

      Fixed<S> round() const {
         Fixed<S> result;
         if (x < 0) {
            result.x = -x + 0.5;
            result.x = -result.x;
         } else {
            result.x = x + 0.5;
         }
         return result;
      }

      Float<S> sin() const {
         Float<S> result;
         result.x = std::sin(x);
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Float<S> cos() const {
         Float<S> result;
         result.x = std::cos(x);
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Float<S> tan() const {
         Float<S> result;
         result.x = std::tan(x);
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Float<S> atan() const {
         Float<S> result;
         result.x = std::atan(x);
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Float<S> tanh() const {
         Float<S> result;
         result.x = std::tanh(x);
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Float<S> exp() const {
         Float<S> result;
         result.x = std::exp(x);
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      Float<S> ln() const {
         Float<S> result(0);
         if (x<=0 ) {
	    throw theFunctionParameterOutOfRangeException;
         }
         result.x = std::log(x);
         FloatHelper::testFloatResult(result.x);

         return result;
      }

      Float<S> sqrt() const {
         Float<S> result;
         if (x<0 ) {
	    throw theFunctionParameterOutOfRangeException;
         }

         result.x = std::sqrt(x);
         FloatHelper::testFloatResult(result.x);
         return result;
      }

      template <int P>Float<S> pow(const Fixed<P>& rhs)const {
         Float<S> result(1);
         Float<S> base(x);
         Float<S> one(1);
         int64_t exp = rhs.x;
         if (exp == 0) {
            result.x = 1;
         } else {
            if (exp < 0) {
               exp = -exp;
               base = one / base;
            }

            while (exp) {
               if (exp & 1) {
                  result = result * base;
               }
               exp >>= 1;
               if (exp) {
                  base = base * base;
               }
            }
         }
         return result;
      }
   };

   template <int S, int T> Float<T>
   operator+(const Fixed<S> & lhs, const Float<T> & rhs) {
      Float<T> result;
      result.x = lhs.x + rhs.x;
      FloatHelper::testFloatResult(result.x);
      return result;
   }

   template <int S, int T> Float<T>
   operator-(const Fixed<S> & lhs, const Float<T> & rhs) {
      Float<T> result;
      result.x = lhs.x - rhs.x;
      FloatHelper::testFloatResult(result.x);
      return result;
   }

   template <int S, int T> Float<T>
   operator*(const Fixed<S> & lhs, const Float<T> & rhs) {
      Float<T> result;
      result.x = lhs.x * rhs.x;
      FloatHelper::testFloatResult(result.x);
      return result;
   }

   template <int S, int T> Float<T>
   operator/(const Fixed<S> & lhs, const Float<T> & rhs) {
      Float<T> result;
      result.x = lhs.x / rhs.x;
      FloatHelper::testFloatResult(result.x);
      return result;
   }

}

namespace pearlrt {
   class Duration;
}

namespace pearlrt {


   class Duration {
   private:
      Fixed63 intval;
   public:

      Duration();

      Duration(const int64_t sec, const int us, const int sign=1);

      Fixed63 get() const;

      int64_t getSec() const;

      int getUsec(void) const;

      Duration& operator+=(const Duration& rhs);

      Duration operator+(const Duration& rhs) const;

      Duration& operator-=(const Duration& rhs);

      Duration operator-(const Duration& rhs) const;

      Clock operator+(const Clock& d) const;

      Duration operator-() const;

      template <int S>Duration& operator*=(const Fixed<S>& rhs) {
         Fixed63 r((Fixed63::Fixed63_t)(rhs.x));
         intval = intval * r;
         return *this;
      }

      template<int S>Duration  operator*(const Fixed<S>& rhs) const {
         return Duration(*this) *= rhs;
      }

      template <int S>Duration& operator/=(const Fixed<S>& rhs) {
         if ((rhs == Fixed<S>(0.0)).getBoolean()) {
            throw theDurationDivideByZeroSignal;
         }
         Fixed63 help;
         try {
            help = intval.get() / rhs.x;
         } catch (FloatIsINFSignal &s) {
            throw theDurationRangeSignal;
         }
         intval = help;
         return (*this);
      }

      template <int S>Duration operator/(const Fixed<S>& rhs) {
         return Duration(*this) /= rhs;
      }

      template <int S>Duration& operator*=(const Float<S>& rhs) {
         intval = (double)(intval.get()) * rhs.x;
         return *this;
      }

      template<int S>Duration  operator*(const Float<S>& rhs) const {
         return Duration(*this) *= rhs;
      }

      template <int S>Duration& operator/=(const Float<S>& rhs) {
         if ((rhs == Float<S>(0.0)).getBoolean()) {
            throw theDurationDivideByZeroSignal;
         }
         Float<S> help;
         try {
            help = Float<S>(intval.get()) / rhs;
         } catch (FloatIsINFSignal &s) {
            throw theDurationRangeSignal;
         }
         intval = Fixed63(help.x);
         return (*this);
      }

      template <int S>Duration operator/(const Float<S>& rhs) {
         return Duration(*this) /= rhs;
      }

      Float<23> operator/=(const Duration& rhs);

      Float<23> operator/(const Duration& rhs) const;

      int compare(const Duration& rhs) const;

      BitString<1> operator==(const Duration& rhs) const;

      BitString<1> operator!=(const Duration& rhs) const;

      BitString<1> operator>(const Duration& rhs) const;

      BitString<1> operator<(const Duration& rhs) const;

      BitString<1> operator>=(const Duration& rhs) const;

      BitString<1> operator<=(const Duration& rhs) const;

      Duration abs() const;

      Fixed<1> sign() const;
   };

   template<int S>Duration  operator*(const Fixed<S>& lhs,
         const Duration &rhs) {
      Duration result(rhs);
      result *= lhs;
      return result;
   }

   template<int S>Duration  operator*(const Float<S>& lhs,
         const Duration &rhs) {
      Duration result(rhs);
      result *= lhs;
      return result;
   }
}
namespace pearlrt {


   class Clock {
   private:
      Fixed63 intval;

      void adjust();
   public:

      static Clock now(void);

      Clock();

      Clock(double x);

      int getSec(void) const;

      int getUsec(void) const;

      const Fixed63& get(void) const;

      Clock operator+(const Duration& d);

      Clock operator-(const Duration& d);

      Duration operator-(const Clock& c);

      int compare(const Clock& rhs) const;

      BitString<1> operator<(const Clock& rhs) const;

      BitString<1> operator<=(const Clock& rhs) const;

      BitString<1> operator==(const Clock& rhs) const;

      BitString<1> operator!=(const Clock& rhs) const;

      BitString<1> operator>=(const Clock& rhs) const;

      BitString<1> operator>(const Clock& rhs) const;
   };
}


#define INTERRUPT_INCLUDED

#define TASKWHENLINKS_INCLUDED
namespace pearlrt {


   class TaskWhenLinks {
   protected:

      TaskWhenLinks* nextActivate;

      TaskWhenLinks* nextContinue;
   public:

      TaskWhenLinks* getNextContinue();

      void setNextContinue(TaskWhenLinks * next);

      TaskWhenLinks* getNextActivate();

      void setNextActivate(TaskWhenLinks * next);

      virtual void triggeredContinue() = 0;

      virtual void triggeredActivate() = 0;
   };
}

#define MUTEX_INCLUDED


#define IMUTEXCOMMON_INCLUDED
namespace pearlrt {


   class MutexCommon {
   protected:

      const char * id;
   public:

      MutexCommon();

      void name(const char * s);

      const char * getName();


      virtual void lock() = 0;

      virtual void unlock() = 0;
   };
}
namespace pearlrt {


   class Mutex : public MutexCommon {
   private:
      pthread_mutex_t mutex;
   public:

      Mutex();

      ~Mutex();

      void lock();

      void unlock();
   };

}
namespace pearlrt {



   class Interrupt {
   private:
      TaskWhenLinks * headContinueTaskQueue;
      TaskWhenLinks * headActivateTaskQueue;
      bool         isEnabled;
      Mutex	mutexOfInterrupt;
   public:

      Interrupt();

      void enable();

      void disable();

      void trigger();

      void registerActivate(TaskWhenLinks* t, TaskWhenLinks **next);

      void registerContinue(TaskWhenLinks* t, TaskWhenLinks **next);

      void unregisterActivate(TaskWhenLinks* t);

      void unregisterContinue(TaskWhenLinks* t);
      protected:

      virtual void devEnable()=0;

      virtual void devDisable()=0;
   };

}



#define TASKCOMMON_INCLUDED

namespace pearlrt {

   class TaskCommon;
   class Semaphore;
   class Bolt;
   class UserDation;

   enum BlockReason {
      NOTBLOCKED,
      REQUEST,
      ENTER,
      RESERVE,
      IO,
      IOWAITQUEUE,
      IO_MULTIPLE_IO
   };

   struct BlockData {
      BlockReason reason;

      union BlockReasons {

         struct BlockSema {
            int nsemas;
            Semaphore **semas;
         } sema;

         struct BlockBolt {
            int nbolts;
            Bolt **bolts;
         } bolt;

         struct BlockIO {

            Dation::DationParams direction;
            UserDation* dation;
         } io;
      } u;
   };
}








namespace pearlrt {
   class UserDation;
}
#define USERDATION_INCLUDED



#define RST_INCLUDED


namespace pearlrt {


    class Rst {
        protected:

            void * rstVoidPointer;

            size_t rstLength;
        public:

            Rst();

            template <int SIZE>
            void rst(Fixed<SIZE> & rstVariable) {
                rst(&rstVariable, SIZE);
            }

            void rst(void * rstVoidPointer, size_t len);

            bool updateRst(Signal * s);
    };

}







#define PRIORITYQUEUE_INCLUDED
namespace pearlrt {
   class TaskCommon ;
}
namespace pearlrt {


   class PriorityQueue {
   private:
      TaskCommon* head;
   public:

      PriorityQueue();

      void insert(TaskCommon * x);

      bool remove(TaskCommon * x);

      TaskCommon* getHead();

      TaskCommon* getNext(TaskCommon * x);
   };
}
namespace pearlrt {

   class UserDation : public Dation, public Rst {

   protected:

      Mutex mutexUserDation;

      DationParams currentDirection;

      SystemDation * systemDation;

      PriorityQueue waitQueue;

      bool isBusy;

      Fixed<31> counter;
   private:
      static const int maxLengthIdfName = 64;
      Character<maxLengthIdfName> idfNameStorage;
      bool idfNameGiven;
   protected:
      RefCharacter idfName;

   public:

      UserDation();
   private:

      virtual void internalDationOpen(int p, RefCharacter * newFilename) = 0;
   public:

      template <int R>
      void dationOpen(int p,
                      RefCharacter* idf,
                      Fixed<R> * rst) {
         mutexUserDation.lock();

         try {
            if (p & RST) {
               if (! rst) {
                  Log::error("UserDation: RST is set but no"
                             " variable given");
                  throw theInternalDationSignal;
               }
               *rst = 0;
            }
            if ((!!(p & Dation::IDF)) != (idf != 0)) {
               Log::error("UserDation: ether both or non of IDF and filename");
               throw theInternalDationSignal;
            }
            internalDationOpen(p, idf );
         } catch (Signal & s) {
            if (rst) {
               try {

                  *rst = s.whichRST();
               } catch (Signal & s) {
                  mutexUserDation.unlock();
                  throw;
               }
            } else {
               mutexUserDation.unlock();
               throw;
            }
         }
         mutexUserDation.unlock();
      }
   private:
      void internalDationClose(const int  p = 0);
   public:

      template<int S> void dationClose(const int  p, Fixed<S> * rst) {
         Fixed<S>* intRst = NULL;
         mutexUserDation.lock();
         try {
            if (p & RST) {
               if (! rst) {
                  Log::error("UserDation: RST is set but no variable given");
                  throw theInternalDationSignal;
               }
               *rst = 0;
               intRst  = rst;
            }

            internalDationClose(p);
         } catch (Signal &  s) {
            if (intRst != NULL) {
               try {
                  *intRst = (Fixed<31>)s.whichRST();
               } catch (Signal & s) {
                  mutexUserDation.unlock();
                  throw;
               }
            } else {
               mutexUserDation.unlock();
               throw;
            }
         }
         mutexUserDation.unlock();
      }

      virtual void closeSystemDation(int dationParams) = 0;
   protected:

      void assertOpen();
   public:

      void restart(TaskCommon * me,
                         Dation::DationParams dir);

      void beginSequence(TaskCommon * me,
                         Dation::DationParams dir);

      void endSequence(TaskCommon * me,
                         Dation::DationParams dir);

      PriorityQueue* getWaitQueue();
   protected:

      virtual void beginSequenceHook(TaskCommon* me,
                                     Dation::DationParams dir) = 0;

      virtual void endSequenceHook(Dation::DationParams dir) = 0;
   public:

      void suspend(TaskCommon * ioPerformingTask);

      void terminate(TaskCommon * ioPerformingTask);

      DationParams getCurrentDirection();

      void checkOpenParametersAndIncrementCounter(int p, RefCharacter * newFilename, SystemDation * parent);

      void checkCloseParametersAndDecrementCounter(int p, SystemDation * parent);
   };
}


#define TASKTIMERCOMMONINCLUDED




namespace pearlrt {


   class TaskTimerCommon {
   protected:

      typedef void (*TimerCallback)(TaskCommon*);
      int counts;
      int countsBackup;
      TaskCommon* task;
      TimerCallback callback;
#if 0
      timer_t timer;
      struct itimerspec its;
#endif
   public:

      TaskTimerCommon();

      void set(
         int condition,
         Clock at,
         Duration after,
         Duration all,
         Clock until,
         Duration during);
      virtual void setTimer(int condition,
            Duration after, Duration all, int count) = 0;

      int cancel();

      int start();
      virtual int startTimer() = 0;

      bool isActive();

      bool isSet();
   private:

      int stop();
      virtual int stopTimer()=0;

   public:

      void update();

      void detailedStatus(char * id, char * line);
   };

}

namespace pearlrt {

   class TaskCommon : public TaskWhenLinks {
   protected:

      void testScheduleCondition(int condition, Duration during, Duration all);
   private:
      static CSema mutexTasks;
   protected:

      CSema activateDone;

      struct Schedule {
         bool whenRegistered;
         Interrupt * when;
         Fixed<15> prio;
         TaskTimerCommon * taskTimer;
      };

      Schedule schedActivateData;

      Schedule  schedContinueData;

      bool schedActivateOverrun;

      volatile bool asyncTerminateRequested;

      volatile bool asyncSuspendRequested;
   public:

      enum TaskState {

         TERMINATED,

         RUNNING,

         SUSPENDED,

         SUSPENDED_BLOCKED,

         BLOCKED
      };

      enum TaskScheduling {
         AT = 1, AFTER = 2, WHEN = 4, ALL = 8,
         DURING = 16, UNTIL = 32, PRIO = 64
      };
   protected:
      Fixed<15> defaultPrio;

      Fixed<15> currentPrio;

      char * name;
      int isMain;
      enum TaskState taskState;

      int sourceLine;
      const char * fileName;

      struct BlockParams {
         CSema semaBlock;

         TaskCommon * next;

         BlockData why;
      } blockParams;

      CSema suspendDone;

      int suspendWaiters;

      CSema continueDone;

      int continueWaiters;

      CSema terminateDone;

      int terminateWaiters;

      TaskCommon(char * n, Prio prio, BitString<1> isMain);
   public:

      void suspend(TaskCommon* me);

      void cont(TaskCommon* me,
                int condition = 0,
                Prio prio = Prio(),
                Clock at = 0.0,
                Duration after = Duration(0,0),
                Interrupt* when = 0);

      void activate(TaskCommon * me,
                    int condition = 0,
                    Prio prio = Prio(),
                    Clock at = 0.0,
     Duration after = Duration(0,0),
                    Duration all = Duration(0,0),
                    Clock until = 0.0,
                    Duration during = Duration(0,0),
                    Interrupt * when = 0);

      void scheduledActivate(int condition,
                             Fixed<15>& prio,
                             Clock& at, Duration& after,
                             Duration& all, Clock& until,
                             Duration& during,
                             Interrupt* when);

      void terminate(TaskCommon * me);

      void resume(int condition,
                  Clock at = 0.0,
                  Duration after = Duration(0,0),
                  Interrupt* when = 0);

      void prevent(TaskCommon * me);

      char * getName();

      int getIsMain();

      TaskState getTaskState();

      Fixed<15> getPrio();
   public:

      virtual void scheduleCallback(void);

      void setLocation(int lineNumber, const char * fileName);

      const char* getLocationFile();

      int getLocationLine();

      void block(BlockData * why);

      void unblock();

      void getBlockingRequest(BlockData *why);

      TaskCommon* getNext();

      void setNext(TaskCommon*t);

      static void mutexLock();

      static void mutexUnlock();

      void enterIO(UserDation * d);

      void leaveIO();

      bool isMySelf(TaskCommon * me);

      static void activateHandler(TaskCommon * tsk);

      void timedActivate();

      static void continueHandler(TaskCommon * tsk);

      void timedContinue();

      void triggeredContinue();

      void triggeredActivate();

      void terminateFromOtherTask();

      void changeThreadPrio(const Fixed<15>& prio);

      void doAsyncSuspend();

      void doAsyncTerminate();


      char* getTaskStateAsString();

      int detailedTaskState(char * lines[3]);

      static char * getCallingTaskName();
   private:

      void continueFromOtherTask(int condition,
                                 Prio prio);
   public:


      virtual void directActivate(const Fixed<15>& prio) = 0;

      virtual void terminateMySelf() = 0;

      virtual void terminateIO() = 0;

      virtual void terminateSuspended() = 0;

      virtual void terminateSuspendedIO() = 0;

      virtual void terminateRunning() = 0;

      virtual void suspendMySelf() = 0;

      virtual void suspendRunning() = 0;

      virtual void suspendIO() = 0;

      virtual void continueSuspended() = 0;

      virtual void setPearlPrio(const Fixed<15>& prio) = 0;
   };
}

#define TASKTIMERINCLUDED


namespace pearlrt {


   class TaskTimer : public TaskTimerCommon {
   public:

   private:
      int signalNumber;
      timer_t timer;
      struct itimerspec its;
   protected:
      void setTimer(int condition,
            Duration after, Duration all, int count);
      int startTimer();
      int stopTimer();
   public:

      static void init(int p);

      void create(TaskCommon * task, int signalNumber, TimerCallback cb);
      void detailedStatus(char*id, char* line);
   };

}
namespace pearlrt {


   class Task : public TaskCommon {
   private:
      static int useDefaultSchedulerFlag;
      static int schedPrioMax;
      static int numberOfCores;
      Task();

      cpu_set_t  * cpuset;
   public:

      void scheduleCallback(void);
      void (*body)(Task * me);
   private:
#if 0

      CSema suspendDone;
      int suspendWaiters;

      CSema continueDone;
      int continueWaiters;

      CSema terminateDone;
      int terminateWaiters;
#endif

      int pipeResume[2];

      pthread_t threadPid;
      pthread_attr_t attr;
      struct sched_param param;
      TaskTimer activateTaskTimer;
      TaskTimer continueTaskTimer;




   public:
   private:

      void suspendMySelf();

      void terminateMySelf();

      void terminateFromOtherTask();
   public:
      void terminateIO();
      void terminateSuspended();
      void terminateSuspendedIO();
      void terminateRunning();

      Task(void (*body)(Task*), char * n, Prio prio,
           BitString<1> isMain);
      void suspendRunning();
      void suspendIO();

      void continueSuspended();

      void entry();
   private:

      void directActivate(const Fixed<15>& prio);
   public:

      pthread_t getPid();


      static void useDefaultScheduler();

      static void setNumberOfCores(int nbrOfCores);

      static void setThreadPrioMax(int p);
      static int getThreadPrioMax();

      void switchToThreadPrioMax();

      void switchToThreadPrioCurrent();

      void setPearlPrio(const Fixed<15>& prio);

      void setThreadPrio(int p);

      static Task* currentTask(void);

      void treatCancelIO(void);

      static bool delayUs(uint64_t usecs);

      void setCpuSet(cpu_set_t *set);

      cpu_set_t * getCpuSet();

      static void getCpuSetAsText(cpu_set_t * set, char* setAsText, size_t size);
   private:
      void enableCancelIOSignalHandler(void);
   };
}
#if 0
#define SPCTASK(t) \
extern pearlrt::Task t;
#endif

#define DCLTASK(x, prio, ismain) 	\
   static void x ## _body (pearlrt::Task * me) ;        \
          pearlrt::Task task ## x ( x ## _body, ((char*)#x)+1,  \
                       prio, ismain);	                \
static void x ## _body (pearlrt::Task * me)  \




#define TASKMONITOR_INCLUDED

namespace pearlrt {


   class TaskMonitor {
   private:
      volatile int nbrPendingTasks;
      Mutex mutex;
      TaskMonitor();
      TaskMonitor(TaskMonitor const&);
      TaskMonitor& operator= (TaskMonitor const&);
      typedef void ExitCallback(void);

      ExitCallback * exitCallback;
   public:

      static TaskMonitor& Instance();

      void incPendingTasks();

      void decPendingTasks();

      int getPendingTasks();

      void setExitCallback(ExitCallback *cb);

      ExitCallback* getExitCallback();
   };
}

#define ARRAY_H_INCLUDED






namespace pearlrt {
#define LIMITS(...) __VA_ARGS__
#define DCLARRAY(name,dimensions,limits) \
   pearlrt::ArrayDescriptor<dimensions> a_##name = { dimensions, limits }; \

   template <int DIM>
   struct ArrayDescriptor {

      int dim;

      struct Limits {

         int low;

         int high;

         int size;
      } lim[DIM];

      size_t offset(Fixed<31> i, ...) {
        size_t result;
        va_list args;
        va_start(args,i);
        result = ((ArrayDescriptor<0>*)this)->_offset(i,args);
        return (result);
      }

      size_t _offset(Fixed<31> i, va_list args);

      Fixed<31> upb(Fixed<31> x);

      Fixed<31> lwb(Fixed<31> x);
      template <int D2>
      bool operator==(const ArrayDescriptor<D2> & rhs);
      template <int D2>
      bool operator!=(const ArrayDescriptor<D2> & rhs);
   };

   template <class C>
   class Array {
   private:
      ArrayDescriptor<0>* descriptor;
      C * data;
      Array() {};
   public:

      Array(ArrayDescriptor<0> * descr, C* d) : descriptor(descr), data(d) {}

      C* getPtr(Fixed<31> i, ...) {
        va_list args;
        va_start(args,i);
        C* result = data+descriptor->_offset(i,args);
        va_end(args);
        return (result);
      }


      ArrayDescriptor<0> * getDescriptor() {
          return descriptor;
      }

     void* getDataPtr() {
         return data;
     }

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


#define PUTCLOCK_H_INCLUDED


#define SINK_H_INCLUDED
namespace pearlrt {


   class Sink {
   public:

      virtual void putChar(char c)  = 0;
   };
}

namespace pearlrt {


   class PutClock {
   public:

      static void toT(const Clock & x,
                      const Fixed<31>& w, const Fixed<31>& d,
                      Sink& sink) ;
   };

}

#define GETCLOCK_H_INCLUDED




#define SOURCE_H_INCLUDED
namespace pearlrt {


   class Source {
   private:
      char unGetCharacter;
      bool unGetCharacterAvailable;
   public:

      Source();

      char getChar(void);

      virtual char realGetChar(void) = 0;

      void unGetChar(char c);

      void forgetUnGetChar(void);
   };
}



namespace pearlrt {


   class GetClock {
   public:

      static int fromT(Clock&c,
                       const Fixed<31> w,
                       const Fixed<31> d,
                       Source & source);
   };

}


#define PUTDURATION_H_INCLUDED



namespace pearlrt {



   class PutDuration {
   public:

      static void toD(const Duration& dur,
                      const Fixed<31>& w,
                      const Fixed<31>& d,
                      Sink& s);
   };

}

#define GETDURATION_H_INCLUDED





namespace pearlrt {


   class GetDuration {
   public:

      static int fromD(Duration&c,
                       const Fixed<31> w,
                       const Fixed<31> d,
                       Source & source);
   };

}



#define PUTFIXED_H_INCLUDED





#define COMPARE_H_INCLUDED





#define CHARSLICE_INCLUDED



namespace pearlrt {

   class CharSlice {
   public:

      char *data;

      Fixed<15> length;

      template<size_t S>CharSlice(Character<S>& source) {
         length = Fixed<15>(S);
         data   = source.get();
      }

      template<size_t S>
      Character<S>* mkCharPtr(Character<S>* dummy) {
         if ((int)S > length.x) {
            printf("mkCharacter: illegal length %d <-> %d", (int)S, length.x);
            throw theCharacterTooLongSignal;
         }
         return (Character<S>*) data;
      }

      CharSlice& getSlice(Fixed<15> start, Fixed<15> end);

      void setSlice(const CharSlice& rhs);

      template<size_t S>
      void setSlice(Character<S> rhs) {
         setSlice(CharSlice(rhs));
      }

      Fixed<15> upb() const;

      Character<1>  getCharAt(Fixed<15> p);

      void setCharAt(Fixed<15> p, Character<1> c);

      Fixed<8> toFixed();

      char *  get();
   };
};


namespace pearlrt {




   template<int S, int P>
   BitString<1> operator== (const Fixed<S> lhs, const Fixed<P> & rhs)  {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return BitString<1>(lhs.x == rhs.x);
   }

   template<int S, int P>
   BitString<1> operator!= (const Fixed<S> lhs, const Fixed<P> & rhs)  {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return BitString<1>(lhs.x != rhs.x);
   }

   template<int S, int P>
   BitString<1> operator< (const Fixed<S> lhs, const Fixed<P> & rhs)  {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return BitString<1>(lhs.x < rhs.x);
   }

   template<int S, int P>
   BitString<1> operator<= (const Fixed<S> lhs, const Fixed<P> & rhs)  {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return BitString<1>(lhs.x <= rhs.x);
   }

   template<int S, int P>
   BitString<1> operator> (const Fixed<S> lhs, const Fixed<P> & rhs)  {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return BitString<1>(lhs.x > rhs.x);
   }

   template<int S, int P>
   BitString<1> operator>= (const Fixed<S> lhs, const Fixed<P> & rhs)  {
      Fixed < (S > P) ? S : P > l(lhs.x), r(rhs.x);
      return BitString<1>(lhs.x >= rhs.x);
   }




   template <int S, int T, template<int> class R>
   BitString<1> operator<(const Float<S> & lhs, const R<T> & rhs) {
      return lhs.x < rhs.x;
   }

   template <int S, int T, template<int> class R>
   BitString<1> operator<=(const Float<S> & lhs, const R<T> & rhs) {
      return lhs.x <= rhs.x;
   }

   template <int S, int T, template<int> class R>
   BitString<1> operator!=(const Float<S> & lhs, const R<T> & rhs) {
      return lhs.x != rhs.x;
   }

   template <int S, int T, template<int> class R>
   BitString<1> operator==(const Float<S> & lhs, const R<T> & rhs) {
      return lhs.x == rhs.x;
   }

   template <int S, int T, template<int> class R>
   BitString<1> operator>=(const Float<S> & lhs, const R<T> & rhs) {
      return lhs.x >= rhs.x;
   }

   template <int S, int T, template<int> class R>
   BitString<1> operator>(const Float<S> & lhs, const R<T> & rhs) {
      return lhs.x > rhs.x;
   }

   template <int S, int T>
   BitString<1> operator<(const Fixed<S> & lhs, const Float<T> & rhs) {
      return lhs.x < rhs.x;
   }

   template <int S, int T>
   BitString<1> operator<=(const Fixed<S> & lhs, const Float<T> & rhs) {
      return lhs.x <= rhs.x;
   }

   template <int S, int T>
   BitString<1> operator!=(const Fixed<S> & lhs, const Float<T> & rhs) {
      return lhs.x != rhs.x;
   }

   template <int S, int T>
   BitString<1> operator==(const Fixed<S> & lhs, const Float<T> & rhs) {
      return lhs.x == rhs.x;
   }

   template <int S, int T>
   BitString<1> operator>=(const Fixed<S> & lhs, const Float<T> & rhs) {
      return lhs.x >= rhs.x;
   }

   template <int S, int T>
   BitString<1> operator>(const Fixed<S> & lhs, const Float<T> & rhs) {
      return lhs.x > rhs.x;
   }




   template<size_t S, size_t P>
   BitString<1> operator== (const Character<S> & lhs,
                            const Character<P> & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, P);
      return BitString<1>(r == 0);
   }

   template<size_t S, size_t P>
   BitString<1> operator!= (const Character<S> & lhs,
                            const Character<P> & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, P);
      return BitString<1>(r != 0);
   }

   template<size_t S, size_t P>
   BitString<1> operator< (const Character<S> & lhs,
                           const Character<P> & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, P);
      return BitString<1>(r < 0);
   }

   template<size_t S, size_t P>
   BitString<1> operator<= (const Character<S> & lhs,
                            const Character<P> & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, P);
      return BitString<1>(r <= 0);
   }

   template<size_t S, size_t P>
   BitString<1> operator> (const Character<S> & lhs,
                           const Character<P> & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, P);
      return BitString<1>(r > 0);
   }

   template<size_t S, size_t P>
   BitString<1> operator>= (const Character<S> & lhs,
                            const Character<P> & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, P);
      return BitString<1>(r >= 0);
   }




   template<size_t P>
   BitString<1> operator== (const CharSlice & lhs,
                            const Character<P> & rhs) {
      int r = characterCompare(lhs.data, lhs.length.x, rhs.data, P);
      return BitString<1>(r == 0);
   }

   template<size_t P>
   BitString<1> operator!= (const CharSlice & lhs,
                            const Character<P> & rhs) {
      int r = characterCompare(lhs.data, lhs.length.x, rhs.data, P);
      return BitString<1>(r != 0);
   }

   template<size_t P>
   BitString<1> operator< (const CharSlice & lhs,
                           const Character<P> & rhs) {
      int r = characterCompare(lhs.data, lhs.length.x, rhs.data, P);
      return BitString<1>(r < 0);
   }

   template<size_t P>
   BitString<1> operator<= (const CharSlice & lhs,
                            const Character<P> & rhs) {
      int r = characterCompare(lhs.data, lhs.length.x, rhs.data, P);
      return BitString<1>(r <= 0);
   }

   template<size_t P>
   BitString<1> operator> (const CharSlice & lhs,
                           const Character<P> & rhs) {
      int r = characterCompare(lhs.data, lhs.length.x, rhs.data, P);
      return BitString<1>(r > 0);
   }

   template<size_t P>
   BitString<1> operator>= (const CharSlice & lhs,
                            const Character<P> & rhs) {
      int r = characterCompare(lhs.data, lhs.length.x, rhs.data, P);
      return BitString<1>(r >= 0);
   }




   template<size_t S>
   BitString<1> operator== (const Character<S> & lhs,
                            const CharSlice & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, rhs.length.x);
      return BitString<1>(r == 0);
   }

   template<size_t S>
   BitString<1> operator!= (const Character<S> & lhs,
                            const CharSlice & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, rhs.length.x);
      return BitString<1>(r != 0);
   }

   template<size_t S>
   BitString<1> operator< (const Character<S> & lhs,
                           const CharSlice & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, rhs.length.x);
      return BitString<1>(r < 0);
   }

   template<size_t S>
   BitString<1> operator<= (const Character<S> & lhs,
                            const CharSlice & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, rhs.length.x);
      return BitString<1>(r <= 0);
   }

   template<size_t S>
   BitString<1> operator> (const Character<S> & lhs,
                           const CharSlice & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, rhs.length.x);
      return BitString<1>(r > 0);
   }

   template<size_t S>
   BitString<1> operator>= (const Character<S> & lhs,
                            const CharSlice & rhs) {
      int r = characterCompare(lhs.data, S, rhs.data, rhs.length.x);
      return BitString<1>(r >= 0);
   }




   BitString<1> operator== (const CharSlice & lhs,
                            const CharSlice & rhs);

   BitString<1> operator!= (const CharSlice & lhs,
                            const CharSlice & rhs);

   BitString<1> operator< (const CharSlice & lhs,
                           const CharSlice & rhs);

   BitString<1> operator<= (const CharSlice & lhs,
                            const CharSlice & rhs);

   BitString<1> operator> (const CharSlice & lhs,
                           const CharSlice & rhs);

   BitString<1> operator>= (const CharSlice & lhs,
                            const CharSlice & rhs);




   template <class C>
   BitString<1> operator== (const Ref<C> & lhs,
                            const Ref<C> & rhs) {
      return BitString<1>(lhs.x == rhs.x);
   }

   template <class C>
   BitString<1> operator== (const C & lhs,
                            const Ref<C> & rhs) {
      return BitString<1>(&lhs == rhs.x);
   }

   template <class C>
   BitString<1> operator== (const Ref<C> & lhs,
                            const C & rhs) {
      return BitString<1>(lhs.x == &rhs);
   }

   template <class C>
   BitString<1> operator== (const Ref<char> & lhs,
                            const Ref<C> & rhs) {
      return BitString<1>((C*)(lhs.x) == rhs.x);
   }

   template <class C>
   BitString<1> operator== (const Ref<C> & lhs,
                            const Ref<char> & rhs) {
      return BitString<1>(lhs.x == (C*)(rhs.x));
   }

   template <class C>
   BitString<1> operator!= (const Ref<C> & lhs,
                            const Ref<C> & rhs) {
      return BitString<1>(lhs.x != rhs.x);
   }

   template <class C>
   BitString<1> operator!= (const C & lhs,
                            const Ref<C> & rhs) {
      return BitString<1>(&lhs != rhs.x);
   }

   template <class C>
   BitString<1> operator!= (const Ref<C> & lhs,
                            const C & rhs) {
      return BitString<1>(lhs.x != &rhs);
   }

   template <class C>
   BitString<1> operator!= (const Ref<char> & lhs,
                            const Ref<C> & rhs) {
      return BitString<1>((C*)(lhs.x) != rhs.x);
   }

   template <class C>
   BitString<1> operator!= (const Ref<C> & lhs,
                            const Ref<char> & rhs) {
      return BitString<1>(lhs.x != (C*)(rhs.x));
   }




   BitString<1> operator== (const RefCharacter & lhs,
                            const RefCharacter & rhs);

   template <size_t S>
   BitString<1> operator== (const Character<S> & lhs,
                            const RefCharacter & rhs) {
      return BitString<1>(lhs.data == rhs.getDataPtr());
   }

   template <size_t S>
   BitString<1> operator== (const RefCharacter & lhs,
                            const Character<S> & rhs) {
      return BitString<1>(lhs.getDataPtr() == rhs.data);
   }

   BitString<1> operator== (const Ref<char> & lhs,
                            const RefCharacter & rhs);

   BitString<1> operator== (const RefCharacter & lhs,
                            const Ref<char> & rhs);

   BitString<1> operator!= (const RefCharacter & lhs,
                            const RefCharacter & rhs);

   template <size_t S>
   BitString<1> operator!= (const Character<S> & lhs,
                            const RefCharacter & rhs) {
      return BitString<1>(lhs.data != rhs.getDataPtr());
   }

   template <size_t S>
   BitString<1> operator!= (const RefCharacter & lhs,
                            const Character<S> & rhs) {
      return BitString<1>(lhs.getDataPtr() != rhs.data);
   }


   BitString<1> operator!= (const Ref<char> & lhs,
                            const RefCharacter& rhs);

   BitString<1> operator!= (const RefCharacter & lhs,
                            const Ref<char> & rhs);
}
using namespace std;
namespace pearlrt {



   template<int S> class PutFixed {
   private:

      PutFixed() {}
   public:

      static void toF(
         Fixed<S> fixedValue,
         Fixed<31> w,
         Fixed<31> d,
         Sink & sink) {
         Fixed<S> x, y, absValue;
         int32_t prePointDigits, postPointDigits;
         int32_t leadingSpaces, digits, ch;
         Fixed<31> digitsFixed;
         bool signNeeded;
         bool pointNeeded;

         if (w.x <= 0) {
            Log::info("F: width < 0");
            throw theFixedFormatSignal;
         }
         if ((d < (Fixed<31>)0).getBoolean() || (d >= w).getBoolean()) {
            Log::info("F: width and decimals violation");
            throw theFixedFormatSignal;
         }

         signNeeded = false;
         x = fixedValue;
         if ((x < (Fixed<S>)0).getBoolean()) {
            x = -x;
            signNeeded = true;
         }
         absValue = x;
         pointNeeded = false;
         if (d.x > 0) {
            pointNeeded = true;
         }

         digits = 1;
         y = (Fixed<S>) 10;
         while ((x >= y).getBoolean()) {
            x = x / y;
            digits ++;
         }
         postPointDigits = d.x;
         prePointDigits = digits;
         leadingSpaces = w.x -
                         ((signNeeded ? 1 : 0) +
                          (pointNeeded ? 1 : 0) +
                          prePointDigits +
                          postPointDigits
                         );
         if (leadingSpaces < 0) {
            Log::info("F: width too small");
            throw theFixedValueSignal;
         }
         while (leadingSpaces > 0) {
            sink.putChar(' ');
            leadingSpaces --;
         }
         if (signNeeded) {
            sink.putChar('-');
         }
         digitsFixed = (Fixed<31>)(digits - 1);
         x = y.pow(digitsFixed);

         do {
            if (x.x == 0) {
               ch = 0;
            } else {
               ch = (absValue / x).x;
            }
            sink.putChar(ch + '0');
            absValue = absValue - x * (Fixed<S>)ch;
            x = x / y;
            prePointDigits --;
         } while (prePointDigits > 0);
         if (pointNeeded) {
            sink.putChar('.');
         }
         while (postPointDigits > 0) {
            if (x.x == 0) {
               ch = 0;
            } else {
               ch = (absValue / x).x;
            }
            sink.putChar(ch + '0');
            absValue = absValue - x * (Fixed<S>)ch;
            x = x / y;
            postPointDigits --;
         }
         return;
      }
   };

}

#define GETFIXED_H_INCLUDED





#define GETHELPER_H_INCLUDED







namespace pearlrt {



   class GetHelper {
   private:
      int width;
      Source * source;
      int delimiter;
   public:

      static double pow10(int exp);

      GetHelper(const Fixed<31> w,
                Source * s);

      int getRemainingWidth();

      int skipSpaces();

      void discardRemaining();

      int readInteger(uint64_t * x, const int digits);

      int readFixedInteger(int * x, const int digits);

      int readMantissa(double * x, const int digits, const int decimals);

      int readSeconds(double * x);

      int readString(const char* s);

      enum Delimiters {

         DoubleSpace = 1,

         EndOfLine = 2,

         Comma = 4,

         EndOfFile = 8
      };

      void setDelimiters(int del);

      int readChar();

      void readCharacterByA(RefCharacter * rc);

      void readB4(uint64_t * value, const int nbrOfBitsToSample);

      void readB123(uint64_t * value, const int nbrOfBitsToSample,
                    const int base);

      void readFixedByF(Fixed63 * value, int d);

      void readFloatByF(Float<52> * value, int d);

      void readFloatByE(Float<52> * value);
   };

}
namespace pearlrt {


   template <int S>
   class GetFixed {
   public:

      static void fromF(Fixed<S> &f,
                        const Fixed<31> w,
                        const Fixed<31> d,
                        Source & source) {
         Fixed63 value;
         if (w.x <= 0 || d.x < 0 || w.x < d.x) {
            throw theFixedFormatSignal;
         }
         GetHelper helper(w, &source);
         helper.setDelimiters(GetHelper::EndOfLine);

         helper.readFixedByF(&value, d.x);
         f = Fixed<S>(value.get());
         return;
      }
   };

}

#define PUTFLOAT_H_INCLUDED






#define PUTHELPER_H_INCLUDED




namespace pearlrt {


   class PutHelper {
   public:

      static void doPutChar(int length, RefCharacter* rc, Sink * s);

      const static Float<52> binExpValues[];

      const static int nbrBinExpValues;
   };

}
using namespace std;
namespace pearlrt {


   template<int S> class PutFloat {
   private:

      PutFloat() {}
   public:

      static void toF(
         Float<S> floatValue,
         Fixed<31> w,
         Fixed<31> d,
         Sink & sink) {
         Float<S> x, y, absValue;
         int32_t prePointDigits, postPointDigits;
         int32_t leadingSpaces, digits, ch;
         Fixed<31> digitsFixed;
         bool signNeeded;
         bool pointNeeded;
         static const Float<23> ten(10);

         if (w.x <= 0) {
            Log::info("F: width <= 0");
            throw theFixedFormatSignal;
         }
         if ((d < (Fixed<31>)0).getBoolean() || (d >= w).getBoolean()) {
            Log::info("F: width and decimals violation");
            throw theFixedFormatSignal;
         }

         signNeeded = false;
         x = floatValue;
         if ((x < (Fixed<S>)0).getBoolean()) {
            x = -x;
            signNeeded = true;
         }
         absValue = x;
         pointNeeded = false;
         if (d.x > 0) {
            pointNeeded = true;
         }

         Float<23> half(0.5);
	 for (int s=0; s<d.x; s++) {
            half = half / ten;
         }
         absValue = absValue + half;

         digits = 1;
         y = (Float<S>) 10;
         x = absValue;
         while ((x >= y).getBoolean()) {
            x = x / y;
            digits ++;
         }
         postPointDigits = d.x;
         prePointDigits = digits;
         leadingSpaces = w.x -
                         ((signNeeded ? 1 : 0) +
                          (pointNeeded ? 1 : 0) +
                          prePointDigits +
                          postPointDigits
                         );
         if (leadingSpaces < 0) {
            Log::info("F: width too small");
            throw theFixedValueSignal;
         }
         while (leadingSpaces > 0) {
            sink.putChar(' ');
            leadingSpaces --;
         }
         if (signNeeded) {
            sink.putChar('-');
         }
         digitsFixed = (Fixed<31>)(digits - 1);
         x = y.pow(digitsFixed);

         do {
            if (x.x == 0) {
               ch = 0;
            } else {
               ch = (absValue / x).x;
            }
            sink.putChar(ch + '0');
            absValue = absValue - x * (Fixed<S>)ch;
            x = x / y;
            prePointDigits --;
         } while (prePointDigits > 0);
         if (pointNeeded) {
            sink.putChar('.');
         }
         while (postPointDigits > 0) {
            if (x.x == 0) {
               ch = 0;
            } else {
               ch = (absValue / x).x;
            }
            sink.putChar(ch + '0');
            absValue = absValue - x * (Fixed<S>)ch;
            x = x / y;
            postPointDigits --;
         }
         return;
      }

      static void toE(
         Float<S> floatValue,
         Fixed<31> w,
         Fixed<31> d,
         Fixed<31> s,
         Fixed<31> eSize,
         Sink & sink) {
         Float<52> x, y, testExponent;
         static const Float<52> one(1.0);
         static const Float<52> ten(10.0);

         int32_t leadingSpaces;
         int32_t expValue;
         int32_t i,e;
         int32_t sign=1;
         int32_t prePointDigits, postPointDigits;
         int ch;

         if (w.x <= 0) {
            Log::info("E: width < 0");
            throw theExpFormatSignal;
         }
         if ((s < d+(Fixed<31>)1).getBoolean() ) {
            Log::info("E: significance and decimals violation");
            throw theExpFormatSignal;
         }
         if ( eSize.x != 2 && eSize.x != 3) {
            Log::info("E: exponent field with illegal (%d)", (int)eSize.x);
            throw theExpFormatSignal;
         }





         leadingSpaces = w.x - s.x - 2 - eSize.x;
         if ((d > (Fixed<31>)0).getBoolean() ) {
             leadingSpaces --;
         }
         x = floatValue;
         if ((x < (Float<52>)0).getBoolean()) {
            x = -x;
            sign = -1;
            leadingSpaces --;
         }
         if ((d < (Fixed<31>)0).getBoolean() || leadingSpaces < 0 ) {
            Log::info("E: width and decimals violation");
            throw theExpFormatSignal;
         }

         expValue = 0;
         if ( (x >= one).getBoolean()) {
            if (eSize.x == 2) {
               testExponent = GetHelper::pow10(100);
               if ((x >= testExponent).getBoolean()) {
                  Log::info("E: number too large");
                  throw theExpValueSignal;
               }
            }
            e = 256;
            y = x;
            for (i=0; i<PutHelper::nbrBinExpValues; i++) {
               if ( (y >= PutHelper::binExpValues[i]).getBoolean()) {
                  y = y / PutHelper::binExpValues[i];
                  expValue += e;
               }
               e >>= 1;
            }
         } else {
            e = 256;
            y = x;
            for (i=0; i<PutHelper::nbrBinExpValues; i++) {
               if ( (y * PutHelper::binExpValues[i] < ten).getBoolean()) {
                  y = y * PutHelper::binExpValues[i];
                  expValue += e;
               }
               e >>= 1;
            }
            expValue = -expValue;
         }

         if (expValue < -99) {
            x = (Float<52>)0.0;
            expValue = 0;
            prePointDigits = 1;
            postPointDigits = d.x;

            sign=1;
            prePointDigits = 1;
            leadingSpaces = w.x - 6 - d.x;
         } else {
            prePointDigits = (s.x - d.x);
            postPointDigits = d.x;
            expValue -= (s.x - d.x -1);
         }
         while(leadingSpaces > 0) {
            sink.putChar(' ');
            leadingSpaces --;
         }
         if (sign == -1) {
            sink.putChar('-');
         }
         x = x / Float<52>(GetHelper::pow10(expValue));
         y = GetHelper::pow10(prePointDigits-1);


         x = x + Float<52>(0.5) / Float<52>(GetHelper::pow10(postPointDigits));

         if ((x >= Float<52>(GetHelper::pow10(prePointDigits))).getBoolean()) {
            x = x / ten;
            expValue ++;
         }
         while (prePointDigits > 0) {
            ch = (x / y).x;
            sink.putChar(ch+'0');
            x = x - (Float<52>)ch * y;
            y = y / (Float<52>)10.0;
            prePointDigits --;
         }
         if ((d > (Fixed<31>)0).getBoolean() ) {
            sink.putChar('.');
         }
         y = (Float<52>)10.0;
         while (postPointDigits > 0) {
            x  = (x * y);
            ch = x.x;
            sink.putChar(ch+'0');
            x = x - (Float<52>)ch;
            postPointDigits --;
         }
         sink.putChar('E');
         if (expValue>=0) {
            sink.putChar('+');
         } else {
            sink.putChar('-');
            expValue = - expValue;
         }
         if (eSize.x == 3) {

            ch = expValue / 100 + '0';
            sink.putChar(ch);
            expValue %= 100;
         }
         ch = expValue / 10 + '0';
         sink.putChar(ch);
         ch = expValue % 10 + '0';
         sink.putChar(ch);
      }
   };

}

#define GETFLOAT_H_INCLUDED






namespace pearlrt {


   template <int S>
   class GetFloat {
   public:

      static void fromF(Float<S> &f,
                        const Fixed<31> w,
                        const Fixed<31> d,
                        Source & source) {
         Float<52> value;
         if (w.x <= 0 || d.x < 0 || w.x < d.x) {
            throw theFixedFormatSignal;
         }
         GetHelper helper(w, &source);
         helper.setDelimiters(GetHelper::EndOfLine);

         helper.readFloatByF(&value, d.x);
         f = value.fit(f);
         return;
      }

      static void fromE(Float<S> &f,
                        const Fixed<31> w,
                        const Fixed<31> d,
 	const Fixed<31> s,
                        Source & source) {
         Float<52> value;
         if (w.x <= 0 || d.x < 0 || s.x < 1) {
            throw theExpFormatSignal;
         }
         GetHelper helper(w, &source);
         helper.setDelimiters(GetHelper::EndOfLine);

         helper.readFloatByE(&value);
         f = value.fit(f);
         return;
      }
   };

}




#define PUTCHARACTER_H_INCLUDED






using namespace std;
namespace pearlrt {


   template<size_t S>
   class PutCharacter {
   private:

      PutCharacter() {}
   public:

      static void toA(
         Character<S> &charValue,
         const Fixed<31> w,
         Sink & sink) {
         RefCharacter rc;
         rc.setWork(charValue);

         PutHelper::doPutChar(w.x, &rc, &sink);
      }

      static void toA(
         Character<S> &charValue,
         Sink & sink) {
         toA(charValue, S, sink);
      }
   };

}

#define GETCHARACTER_H_INCLUDED





namespace pearlrt {


   template <size_t S>
   class GetCharacter {
   public:

      static void fromA(Character<S> &c,
                        const Fixed<31> w,
                        Source & source) {
          GetHelper helper(w, &source);
          helper.setDelimiters(GetHelper::EndOfLine);
          RefCharacter rc;
          rc.setWork(c);

          helper.readCharacterByA(&rc);
      }
   };

}


#define SIGNALACTION_INCLUDES




namespace pearlrt {

   class SignalAction {
   private:
      Signal* signal;
      Rst rst;
      bool enabled;
   public:
      SignalAction();

      SignalAction(Signal * s);
      Signal* getSignal();
      void setSignal(Signal* s);


      template <int S>
      void setRstVariable(Fixed<S> * rstVariable) {
	this->rst.rst(rstVariable,S);
      }

      bool updateRst(Signal * s);

      void disable();

      void enable();

      bool isEnabled() ;
   };
}

#define SCHEDULEDSIGNALACTIONS_INCLUDES



namespace pearlrt {

   class ScheduledSignalActions {
   private:
      SignalAction* actions;
      int numberOfActions;
   public:

      ScheduledSignalActions(int nbrOfActions, SignalAction * actions);

      int getActionIndexAndSetRstAndDisableHandler(Signal *s);
      template <int S>
      void setAction(int actionIndex, Signal * s, Fixed<S> & rstVariable){


	   actions[actionIndex-1].setRstVariable(&rstVariable);
	   setAction(actionIndex, s);
      }
      void setAction(int actionIndex, Signal * s);

      void setErrorOrThrow(Signal *s);
   };
}



#define BITSLICE_INCLUDED



namespace pearlrt {

   class BitSlice {
   public:

      void *data;

      uint64_t copyOfData;

      size_t primaryDataLength;

      Fixed<15> length;

      Fixed<15> firstSelectedBit;

      template <int S>
      BitSlice(BitString<S> source) {
         length = Fixed<15>(S);
         primaryDataLength = sizeof(source);
         firstSelectedBit = Fixed<15>(1);
         memcpy(&copyOfData, &source.x, primaryDataLength);
	 data = &copyOfData;
      }

      template <int S>
      BitSlice(BitString<S>* source) {
         length = Fixed<15>(S);
         data   = &(source->x);
         primaryDataLength = sizeof(*source);
         firstSelectedBit = Fixed<15>(1);
      }

      template<int RESLEN>
      BitString<RESLEN> mkBitString(BitString<RESLEN>* dummy) {
         if (RESLEN < length.x) {
            Log::error("assignment to smaller bit string");
            throw theInternalDatatypeSignal;
         }
         if (primaryDataLength == 1) {









            BitString<RESLEN> result(
               (*(uint8_t*)data) >> (8 - firstSelectedBit.x - length.x + 1)
               << (RESLEN - length.x));
            return result;
         } else if (primaryDataLength == 2) {
            BitString<RESLEN> result(
               (*(uint16_t*)data) >> (16 - firstSelectedBit.x - length.x + 1)
               << (RESLEN - length.x));
            return result;
         } else if (primaryDataLength == 4) {
            BitString<RESLEN> result(
               (*(uint32_t*)data) >> (32 - firstSelectedBit.x - length.x + 1)
               << (RESLEN - length.x));
            return result;
         } else {

            BitString<RESLEN> result(
               (*(uint64_t*)data) >> (64 - firstSelectedBit.x - length.x + 1)
               << (RESLEN - length.x));
            return result;
         }
      }

      BitSlice* getSlice(Fixed<15> start, Fixed<15> end) {
         static const Fixed<15> one(1);
         if (start.x < 1 || length.x < end.x ||
               start.x > end.x) {
            Log::error("illegal selection on BIT(%d): .BIT(%d:%d)",
                       length.x, start.x, end.x);
            throw theBitIndexOutOfRangeSignal;
         }
         length = end - start + one;
         firstSelectedBit = firstSelectedBit + start - one;
         return (this);
      }

      template<int RHSSIZE>
      void setSlice(BitString<RHSSIZE> rhs) {
         static const Fixed<15> one(1);





         if (primaryDataLength == 1) {
            ((BitString<8>*)data)->setSlice(firstSelectedBit,
                                            firstSelectedBit + length - one,
                                            rhs);
         } else if (primaryDataLength == 2) {
            BitString<16> help(*(uint16_t*)data);
            help.setSlice(firstSelectedBit,
                                             firstSelectedBit + length - one,
                                             rhs);
            *(uint16_t*)data = help.x;
         } else if (primaryDataLength == 4) {
            ((BitString<32>*)data)->setSlice(firstSelectedBit,
                                             firstSelectedBit + length - one,
                                             rhs);
         } else if (primaryDataLength == 8) {
            ((BitString<64>*)data)->setSlice(firstSelectedBit,
                                             firstSelectedBit + length - one,
                                             rhs);
         }

      }
   };
};

#define PUTBITSTRING_H_INCLUDED


namespace pearlrt {


   template<int S> class PutBits;

   template<> class PutBits<1> {
      public:

         static void toBit(Bits<1>::BitType data, int len,
                           int w, int base, Sink &sink);
   };

   template<> class PutBits<2> {
      public:

         static void toBit(Bits<2>::BitType data, int len,
                           int w, int base, Sink &sink);
   };

   template<> class PutBits<4> {
      public:

         static void toBit(Bits<4>::BitType data, int len,
                           int w, int base, Sink &sink);
   };

   template<> class PutBits<8> {
      public:

         static void toBit(Bits<8>::BitType data, int len,
                           int w, int base, Sink &sink);
   };

   template<int S> class PutBitString {
   private:

      PutBitString() {}

      static const int len=NumberOfBytes<S>::resultBitString;
   public:

      static void toB4(
         BitString<S> &bitstring,
         Fixed<31> w,
         Sink & sink) {
         PutBits<len>::toBit(bitstring.x,S, w.x, 4, sink);
         return;
      }

      static void toB1(
         BitString<S> &bitstring,
         Fixed<31> w,
         Sink & sink) {
         PutBits<len>::toBit(bitstring.x, S, w.x, 1, sink);
         return;
      }

      static void toB2(
         BitString<S> &bitstring,
         Fixed<31> w,
         Sink & sink) {
         PutBits<len>::toBit(bitstring.x, S, w.x, 2, sink);
         return;
      }

      static void toB3(
         BitString<S> &bitstring,
         Fixed<31> w,
         Sink & sink) {
         PutBits<len>::toBit(bitstring.x, S, w.x, 3, sink);
         return;
      }
   };

}

#define GETBITSTRING_H_INCLUDED




using namespace std;
namespace pearlrt {


   template<int S> class GetBits;

   template<> class GetBits<1> {
      public:

         static void fromBit(Bits<1>::BitType& data, int len,
                           int w, int base, Source &source);
   };

   template<> class GetBits<2> {
      public:

         static void fromBit(Bits<2>::BitType& data, int len,
                           int w, int base, Source &source);
   };

   template<> class GetBits<4> {
      public:

         static void fromBit(Bits<4>::BitType& data, int len,
                           int w, int base, Source &source);
   };

   template<> class GetBits<8> {
      public:

         static void fromBit(Bits<8>::BitType& data, int len,
                           int w, int base, Source &source);
   };

   template<int S> class GetBitString {
   private:

      GetBitString() {}

      static const int len=NumberOfBytes<S>::resultBitString;
   public:

      static void fromB123(
         BitString<S> &bitstring,
         const Fixed<31> w,
         const int base,
         Source & source) {
         if (w.x <= 0 ) {
            Log::error("B-format: w<= 0");
            throw theBitFormatSignal;
         }

         GetHelper helper(w, &source);
         helper.setDelimiters(GetHelper::EndOfLine);
         GetBits<len>::fromBit(bitstring.x,S,w.x,base,source);
         return;
      }

      static void fromB4(
         BitString<S> &bitstring,
         const Fixed<31> w,
         Source & source) {
         if (w.x <= 0 ) {
            Log::error("B-format: w<= 0");
            throw theBitFormatSignal;
         }
         GetHelper helper(w, &source);
         helper.setDelimiters(GetHelper::EndOfLine);
         GetBits<len>::fromBit(bitstring.x,S,w.x,4,source);
         return;
      }
   };

}

#define REFCHARSINK_H_INCLUDED


namespace pearlrt {



   class RefCharSink: public Sink {
   private:
      RefCharacter* sinkObj;
   public:

      RefCharSink(RefCharacter & s);

      void putChar(char c);

      void pos(size_t newColumn);


      size_t sop();
   };

}




#define SYSTEMDATIONB_INCLUDED




namespace pearlrt{
class SystemDationB: public SystemDation {
public:

    virtual SystemDationB* dationOpen(const RefCharacter * idfValue,
                                       int openParam) = 0;

	virtual void dationClose(int parameters=0)=0;

	virtual void dationWrite(void * start, size_t size)=0;

	virtual void dationRead(void * start, size_t size)=0;
};
}

namespace pearlrt {
   class UserDation;
}

#define DATIONPG_INCLUDED

#define USERDATIONNB_INCLUDED


namespace pearlrt {
   class UserDation;
}





#define DATIONDIM_INCLUDED


namespace pearlrt {


   class DationDim {
   public:

      virtual Fixed<31> getIndex() const = 0;

      Fixed<31> getCapacity() const;

      int getDimensions() const;
   protected:

      DationDim(const Fixed<31> p, const Fixed<31>r, const Fixed<31> c,
                const int d, const bool b);

      const Fixed<31> pages;

      const Fixed<31> rows;

      const Fixed<31> cols;
   private:

      const int dimensions;

      const bool boundedDimension;
   protected:

      Fixed<31> page;

      Fixed<31> row;

      Fixed<31> col;

      static const Fixed<31> one;

      static const Fixed<31> zero;

      int dationParams;
   public:

      void setDationParams(const int p);

      Fixed<31> getColumn() const;

      Fixed<31> getColumns() const;

      Fixed<31> getRow() const;

      Fixed<31> getRows() const;

      Fixed<31> getPage() const;

      Fixed<31> getPages() const;

      bool checkRemainingWidth(Fixed<15> w);

      void reset();

      bool isBounded();

      void gotoNextRecord();
   };

}


#define TFUBUFFER_INCLUDED




namespace pearlrt {



   class TFUBuffer : public Sink, public Source {
   private:
      SystemDationNB* system;
      size_t sizeOfRecord;
      char * record;
      size_t readWritePointer;
      bool containsData;
      int paddingElement;
      bool isAlphicForward;
   public:

      TFUBuffer();

      void flushRecord();


      void readRecord();

      void setAlphicForward(bool isAlphicForward);

      void setSystemDation(SystemDationNB* sys);

      void setupRecord(int recSize, char * rec, int padding);

      void prepare();

      bool isUsed();

      bool isNotEmpty();

      void markEmpty();

      void setPosition(int newPos);

      void write(void * data, size_t n);

      void read(void * data, size_t n);

      void putChar(char c);

      char realGetChar();
   };

}

#define IOJOB_INCLUDED




#define SYSTEMDATIONNBSINK_H_INCLUDED


namespace pearlrt {


   class SystemDationNBSink: public Sink {
   private:

      SystemDationNB* sinkObj;
   public:

      SystemDationNBSink();

      void setSystemDationNB(SystemDationNB * s);

      void putChar(char c);
   };
}
namespace pearlrt {
   class IODataEntry;
   class IOFormatEntry;
   class IOFormatList;


   class LoopControl {
#define MAX_LOOP_LEVEL 11
   private:
      int loopLevel;
      int numberOfItems;
      bool workCyclic;
      int item;
      struct {
         int lastItem;
         int startItem;
         int loops;
         size_t sizeOfBody;
         size_t offsetIncrement;
      } loopControl[MAX_LOOP_LEVEL];
   public:

      LoopControl(int numberOfItems, bool cyclic);

      int next();

      int enter(int nbrOfItems, int nbrOfRepetitions, size_t sizeOfBody=0);

      size_t getOffset();
   };

   class IODataList {
   public:

      size_t nbrOfEntries;

      IODataEntry * entry;
   };

   class IOFormatList {
   protected:

   public:

      size_t nbrOfEntries;

      IOFormatEntry * entry;
   };

   class IODataEntry {
   public:

      enum IODataType  {
         CHAR,
         FLOAT,
         FIXED,
         BIT,

         CHARSLICE,

         REFCHAR,
         CLOCK,
         DURATION,

         LoopStart,

         InduceData,
      };


      struct {

         IODataType baseType: 8;

         unsigned int dataWidth: 24;
      } dataType;

      union {
         void * inData;
         const void * outData;

         size_t   offsetIncrement;
      } dataPtr;

      union {

         int32_t numberOfElements;
      } param1;
      union {

          size_t * end;
          struct {
             Fixed<15> lwb;
             Fixed<15> upb;
          } charSliceLimits;
      } param2;

     size_t getSize();

    size_t getStartOffset();
   };

   class IOFormatEntry {
   public:

      enum IOFormat  {

         A,

         Aw,

         F,

         E2,

         E3,


         B1,

         B2,

         B3,

         B4,

         B1w,

         B2w,

         B3w,

         B4w,

         T,

         D,

         LIST,

         LoopStart,

         IsPositioning,

         RST,

         X,

         SKIP,

         PAGE,

         EOFPOS,

         ADV1,

         ADV2,

         ADV3,


         COL,

         LINE,

         POS1,

         POS2,

         POS3,

         SOP1,

         SOP2,

         SOP3,

         InduceFormat
      };

      IOFormat format;

      union FormatParameter {

         Fixed<31>  f31;

         struct {

            void * voidPtr;

            int  size;
         } fxxPtr;

         int intValue;
      } fp1,
      fp2,
      fp3;
   };

}
namespace pearlrt {

   class UserDationNB: public UserDation {
   protected:

      enum DationType {ALPHIC, TYPE};

      TFUBuffer  tfuBuffer;
   private:
      static const Fixed<31> one;
      static const Fixed<31> zero;
      const DationType dationType;
      char paddingElement;
      void fill(const Fixed<31> n, char fillChar);
      void skipX(const Fixed<31> n);
      void skipUntil(const Fixed<31> n, char fillChar);
      void skipAny(const Fixed<31> n);
      void internalSop(Fixed<31>* page, Fixed<31>* row, Fixed<31>* element);
      Fixed<31> internalAdv(Fixed<31> p, Fixed<31> r, Fixed<31> c);
      void doTfuAndSeekStuff();
   public:

      void markTFUBufferUsed();

      void getTFUBuffer();

      void beginSequenceHook(TaskCommon * me, Dation::DationParams dir);

      void endSequenceHook(Dation::DationParams dir);

      virtual void dationUnGetChar(const char c) = 0;
   public:

      Fixed<31> adv(Fixed<31> c);

      Fixed<31> adv(Fixed<31> r, Fixed<31> c);

      Fixed<31> adv(Fixed<31> p, Fixed<31> r, Fixed<31> c);
   protected:

      DationDim * dim;
   public:

      SystemDationNB* parent;
#if 0

      DationParams currentDirection;

      SystemDationNB* systemDation;
#endif

      Fixed<31> stepSize;

      UserDationNB(SystemDationNB* parent,
                   int & dationParams,
                   DationDim * dimensions,
                   DationType dt);

      void pos(Fixed<31> c);

      void pos(Fixed<31> row, Fixed<31> col);

      void pos(Fixed<31> page, Fixed<31> row, Fixed<31> col);

      void sop(Fixed<31>* element);

      void sop(Fixed<31>* row, Fixed<31>* element);

      void sop(Fixed<31>* page, Fixed<31>* row, Fixed<31>* element);

      void col(Fixed<31> element);

      void line(Fixed<31> r);

      void toAdv(Fixed<31> c);

      void toAdv(Fixed<31> row, Fixed<31> c);

      void toAdv(Fixed<31> page, Fixed<31> row, Fixed<31> col);

      void toX(const Fixed<31> n);

      void toSkip(const Fixed<31> n);

      void toPage(const Fixed<31> n);

      void fromAdv(Fixed<31> c);

      void fromAdv(Fixed<31> row, Fixed<31> c);

      void fromAdv(Fixed<31> page, Fixed<31> row, Fixed<31> col);

      void fromX(const Fixed<31> n);

      void fromSkip(const Fixed<31> n);

      void fromPage(const Fixed<31> n);

      void eof();

      virtual void internalOpen() = 0;
   private:

      void internalDationOpen(int p, RefCharacter* newFilename);
   public:

      void closeSystemDation(int dationParams);

      int toPositioningFormat(TaskCommon * me, IOFormatEntry * jobFormat);

      int fromPositioningFormat(TaskCommon * me, IOFormatEntry * jobFormat);
   protected:

      void assertOpenDirect();

      void assertOpenDirectOrForward();
   };
}




#define SYSTEMDATIONNBSOURCE_H_INCLUDED



namespace pearlrt {


   class SystemDationNBSource: public Source {
   private:

      SystemDationNB * src;
   public:

      SystemDationNBSource();

      void setSystemDationNB(SystemDationNB * s);

      char realGetChar(void);
   };
}

#define IOFORMATS_H_INCLUDED












namespace pearlrt {



   class IOFormats {
   private:
      Sink  *sink;
      Source  *source;
   public:

      void setupIOFormats(Sink * _sink, Source* _source);

      virtual void checkCapacity(Fixed<31> n) = 0;

      template<size_t S>
      void toA(Character<S> & s) {
         toA(s, S);
      };

      template<size_t S>
      void fromA(Character<S> & s) {
         fromA(s, S);
      };

      template<size_t S>
      void toA(Character<S> &s, Fixed<31> w) {
         checkCapacity(w);
         RefCharacter rc;
         rc.setWork(s);

         PutHelper::doPutChar(w.x, &rc, sink);
      }

      void toA(void *s, size_t len, Fixed<31> w);

      void fromA(void *s, size_t len, Fixed<31> w);

      template<size_t S>
      void fromA(Character<S> & s, Fixed<31> w) {
         checkCapacity(w);
         GetCharacter<S>::fromA(s, w, *source);
         return;
      }

      void toBit(void *s, size_t index,
                 size_t len, int base,
                 const Fixed<31> w);

      void fromBit(void *s, size_t index,
                 size_t len, int base,
                 const Fixed<31> w);

      template<int S>
      void toB1(BitString<S>  s, Fixed<31> w = (Fixed<31>)S) {
         checkCapacity(w);
         PutBitString<S>::toB1(s, w, *sink);
      }
#if 0

      template<int S>
      void fromB1(BitString<S> & s, Fixed<31> w = (Fixed<31>)S) {
         checkCapacity(w);
         GetBitString<S>::fromB123(s, w, 1, *source);
         return;
      }
#endif

      template<int S>
      void toB2(BitString<S>  s, Fixed<31> w = (Fixed<31>)((S + 1) / 2)) {
         checkCapacity(w);
         PutBitString<S>::toB2(s, w, *sink);
      }
#if 0

      template<int S>
      void fromB2(BitString<S> & s, Fixed<31> w = (Fixed<31>)((S + 1) / 2)) {
         checkCapacity(w);
         GetBitString<S>::fromB123(s, w, 2, *source);
         return;
      }
#endif

      template<int S>
      void toB3(BitString<S>  s, Fixed<31> w = (Fixed<31>)((S + 2) / 3)) {
         checkCapacity(w);
         PutBitString<S>::toB3(s, w, *sink);
      }
#if 0

      template<int S>
      void fromB3(BitString<S> & s, Fixed<31> w = (Fixed<31>)((S + 2) / 3)) {
         checkCapacity(w);
         GetBitString<S>::fromB123(s, w, 3, *source);
         return;
      }
#endif

      template<int S>
      void toB4(BitString<S>  s, Fixed<31> w = (Fixed<31>)((S + 3) / 4)) {
         checkCapacity(w);
         PutBitString<S>::toB4(s, w, *sink);
      }
#if 0

      template<int S>
      void fromB4(BitString<S> & s, Fixed<31> w = (Fixed<31>)((S + 3) / 4)) {
         checkCapacity(w);
         GetBitString<S>::fromB4(s, w, *source);
         return;
      }
#endif

      template<int S>
      void toF(Fixed<S>  f,
               const Fixed<31> w,
               const Fixed<31> d = 0) {
         checkCapacity(w);
         PutFixed<S>::toF(f, w, d, *sink);
      };

      void toFloatF(void *f, size_t len, size_t index, const Fixed<31> w,
                    const Fixed<31> d = 0) ;

      void fromFloatF(void *f, size_t len, size_t index, const Fixed<31> w,
                      const Fixed<31> d = 0) ;

      void toFloatE(void *f, size_t len, size_t index, const Fixed<31> w,
                    const Fixed<31> d,
                    const Fixed<31> s,
     const int expSize );


      void fromFloatE(void *f, size_t len, size_t index, const Fixed<31> w,
                    const Fixed<31> d,
                    const Fixed<31> s,
     const int expSize );


      void toFixedF(void *f, size_t len, size_t index, const Fixed<31> w,
                    const Fixed<31> d = 0) ;

      void fromFixedF(void *f, size_t len, size_t index, const Fixed<31> w,
                      const Fixed<31> d = 0) ;

      template<int S>
      void fromF(Fixed<S> & f,
                 const Fixed<31> w,
                 const Fixed<31> d = 0) {
         checkCapacity(w);
         GetFixed<S>::fromF(f, w, d, *source);
      };

      template<int S>
      void toF(Float<S>  f,
               const Fixed<31> w,
               const Fixed<31> d = 0) {
         checkCapacity(w);
         PutFloat<S>::toF(f, w, d, *sink);
      };

      template<int S>
      void fromF(Float<S> & f,
                 const Fixed<31> w,
                 const Fixed<31> d = 0) {
         checkCapacity(w);
         GetFloat<S>::fromF(f, w, d, *source);
      };

      template<int S>
      void toE(Float<S>  f,
               const Fixed<31> w,
               const Fixed<31> d,
               const Fixed<31> s,
               const Fixed<31> e) {
         checkCapacity(w);
         PutFloat<S>::toE(f, w, d, s, e, *sink);
      };

      template<int S>
      void fromE(Float<S> & f,
                 const Fixed<31> w,
                 const Fixed<31> d,
                 const Fixed<31> s) {
         checkCapacity(w);
         GetFloat<S>::fromE(f, w, d, s, *source);
      };

      void toT(const Clock f,
               const Fixed<31> w,
               const Fixed<31> d = 0);

      void fromT(Clock & f,
                 const Fixed<31> w,
                 const Fixed<31> d = 0) ;

      void toD(const Duration f,
               const Fixed<31> w,
               const Fixed<31> d = 0);

      void fromD(Duration& f,
                 const Fixed<31> w,
                 const Fixed<31> d = 0);

      int putDataFormat(TaskCommon * me,
                        IODataEntry * dataEntry, size_t index,
                        size_t loopOffset,
                        IOFormatEntry * format);

      int getDataFormat(TaskCommon * me,
                        IODataEntry * dataEntry, size_t index,
                        size_t loopOffset,
                        IOFormatEntry * format);
   };

}








namespace pearlrt {



   class DationPG: public UserDationNB, public IOFormats {
   private:

      void internalOpen();
      SystemDationNBSink  sink;
      SystemDationNBSource   source;
      int formatItem;
      IOFormatList * formatList;
      TaskCommon *me;
      void applyAllPositioningFormats(LoopControl & formatLoop,
             bool directionTo);
   public:

      DationPG(SystemDationNB* parent,
               int dationParams,
               DationDim * dimensions,
               void * tfubuffer = NULL);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationSeek(const Fixed<31> & p, const int dationParam);

      void dationUnGetChar(const char c);

      void checkCapacity(Fixed<31> n);

      void put(TaskCommon *me, IODataList *dataEntries,
               IOFormatList * formatEntries);

      void get(TaskCommon *me, IODataList *dataEntries,
               IOFormatList * formatEntries);
   };

}

#define STRINGDATIONCONVERT_INCLUDED


#define REFCHARSOURCE_H_INCLUDED



namespace pearlrt {



   class RefCharSource: public Source {
   private:

      RefCharacter * sourceObj;
   public:

      RefCharSource(RefCharacter & s);

      char realGetChar(void);

      void rewind();

      void pos(size_t pos);

      size_t sop();
   };

}


namespace pearlrt {


   class StringDationConvert: public IOFormats, public Rst {
   private:
      RefCharacter* string;
      RefCharSink   sink;
      RefCharSource source;
      Fixed<31> currentPosition;
      bool isOutput;
      int formatItem;
      TaskCommon* me;
      IOFormatList * formatList;
   public:

      StringDationConvert(RefCharacter* string, bool isOutput);
     private:

      void dationUnGetChar(const char c);

      void checkCapacity(Fixed<31> n);

      int toPositioningFormat(TaskCommon * me, IOFormatEntry * jobFormat);

      int fromPositioningFormat(TaskCommon * me, IOFormatEntry * jobFormat);

      void applyAllPositioningFormats( LoopControl& formatLoop, bool directionTo);

     public:

      void toX(Fixed<31> n);

      void fromX(Fixed<31> n);

      void adv(Fixed<31> n);

      void pos(Fixed<31> n);

      void sop(Fixed<31> &n);

      void convertTo(TaskCommon *me, IODataList *dataEntries,
               IOFormatList * formatEntries);

      void convertFrom(TaskCommon *me, IODataList *dataEntries,
               IOFormatList * formatEntries);
   };

}

#define DATIONRW_INCLUDED




namespace pearlrt {


   class DationRW: public UserDationNB {
   private:

      void internalOpen();

      void zeroFill(const Fixed<31> n);
   public:

      DationRW(SystemDationNB* parent,
               int dationParams,
               DationDim * dimensions,
               const Fixed<15> stepsize,
               void * tfubuffer = NULL);

      void dationRead(void* data, size_t size);

      void dationWrite(void* data, size_t size);

      void dationSeek(const Fixed<31> & p, const int dationParam);

      void dationUnGetChar(const char c);

      void write(TaskCommon*me,
                 IODataList * dataList, IOFormatList * formatList);

      void read(TaskCommon*me,
                IODataList * dataList, IOFormatList * formatList);
   };

}

#define DATIONTS_INCLUDED

namespace pearlrt {
   class UserDation;
}





namespace pearlrt {


   class DationTS: public UserDation {
   private:

      SystemDationB* system;

      SystemDationB* work;

      void internalOpen();
   private:

      void internalDationOpen(int p, RefCharacter* newFilename);
   public:

      DationTS(SystemDationB* parent,
               int dationParams);

      void closeSystemDation(int dationParams);

      void dationRead(void* data, size_t size);

      void dationWrite(void* data, size_t size);

     void beginSequenceHook(TaskCommon * me, Dation::DationParams dir);


     void endSequenceHook(Dation::DationParams dir);

      bool allowMultipleIORequests();

      void send(TaskCommon*me,
                 IODataList * dataList, IOFormatList * formatList);

      void take(TaskCommon*me,
                IODataList * dataList, IOFormatList * formatList);
   };

}


#define TFURECORD_INCLUDED
namespace pearlrt {



   template <size_t Size>
   class TFURecord {
       private:
          char data[Size] __attribute__ ((aligned(8)));
    };

}



#define DATIONDIM1_INCLUDED



namespace pearlrt {


   class DationDim1 : public DationDim {
   public:

      Fixed<31> getIndex() const;

      DationDim1();

      DationDim1(Fixed<31> c);

      void pos(const Fixed<31> c);

      void adv(const Fixed<31> c);
   };

}

#define DATIONDIM2_INCLUDED



namespace pearlrt {


   class DationDim2 : public DationDim {
   private:

      Fixed<31> getIndex(const Fixed<31> r, const Fixed<31> c) const;
   public:

      Fixed<31> getIndex() const;

      DationDim2(Fixed<31> c);

      DationDim2(Fixed<31> r, Fixed<31> c);

      void pos(const Fixed<31> r, const Fixed<31> c);

      void adv(const Fixed<31> r, const Fixed<31> c);

      Fixed<31> getElements4Skip(const Fixed<31> n);
   };

}

#define DATIONDIM3_INCLUDED



namespace pearlrt {


   class DationDim3 : public DationDim {
   private:

      Fixed<31> getIndex(const Fixed<31> p,
                         const Fixed<31> r,
                         const Fixed<31> c) const;
   public:

      Fixed<31> getIndex() const;

      DationDim3(Fixed<31> r, Fixed<31> c);

      DationDim3(Fixed<31> p, Fixed<31> r, Fixed<31> c);

      void pos(const Fixed<31> p, const Fixed<31> r, const Fixed<31> c);

      void adv(const Fixed<31> p, const Fixed<31> r, const Fixed<31> c);

      Fixed<31> getElements4Skip(const Fixed<31> n);

      Fixed<31> getElements4Page(const Fixed<31> n);
   };

}

#define PRLSEMA_INCLUDED
namespace pearlrt {
   class Semaphore;
}







namespace pearlrt {

   class Semaphore {
   private:
      Semaphore();
      uint32_t value;
      const char * name;
      static PriorityQueue waiters;
      static int internalDoTry(TaskCommon * taskCommon,
                               BlockData::BlockReasons::BlockSema *bd);
   public:

      Semaphore(uint32_t  preset = 0, const char * n = NULL);

      const char* getName(void);

      void decrement(void);

      void increment(void);

      uint32_t getValue(void);
   public:

      static void request(TaskCommon* me,  int nbrOfSemas, Semaphore** semas);

      static BitString<1> dotry(TaskCommon* me,  int nbrOfSemas, Semaphore** semas);

      static void release(TaskCommon* me,  int nbrOfSemas, Semaphore** semas);

      static void removeFromWaitQueue(TaskCommon * t);

      static void addToWaitQueue(TaskCommon * t);

      static void updateWaitQueue(TaskCommon * t);
   };
}

#define PRLBOLT_INCLUDED
namespace pearlrt {
   class Bolt;
}






namespace pearlrt {

   class Bolt {
   private:
      Bolt();
      int state;
      uint32_t nbrOfEnterOperations;
      const char * name;
      static PriorityQueue waiters;
      static int check(BlockReason r, BlockData::BlockReasons::BlockBolt *bd);
      enum BoltStates {FREE = 0, ENTERED = 1, RESERVED = 2};

      void setState(int newState, TaskCommon* taskCommon, int oldState = 0);

      int getState(void);

      static void enterOrReserve(TaskCommon* me,
                                 BlockReason operation,
                                 int newState,  int nbrOfBolts, Bolt** bolts);

      static void leaveOrFree(TaskCommon* me,  int oldState,
                              int nbrOfBolts, Bolt** bolts);
      void incrementEnter();
      void decrementEnter();
      uint32_t getNbrOfEnterOperations();
   public:

      Bolt(const char * n = NULL);

      const char* getName(void);

      static void enter(TaskCommon* me,  int nbrOfBolts, Bolt** bolts);

      static void reserve(TaskCommon* me,  int nbrOfBolts, Bolt** bolts);

      static void leave(TaskCommon* me,  int nbrOfBolts, Bolt** bolts);

      static void free(TaskCommon* me,  int nbrOfBolts, Bolt** bolts);

      static void removeFromWaitQueue(TaskCommon * t);

      static void addToWaitQueue(TaskCommon * t);

      static void updateWaitQueue(TaskCommon * t);

      char* getStateName(void);
   };
}


#define LOGFILE_INCLUDED



namespace pearlrt {

   class LogFile : public SystemDationNB {
      public:

 	 LogFile(SystemDationNB * provider, char * filename);
      private:
	char * logFileName;
        SystemDationNB * provider;

      int cap;

      FILE* fp;

public:

      int capabilities();

      LogFile* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);

      void translateNewLine(bool doNewLineTranslation);
      void suspend(TaskCommon* ioPerformingTask);
      void terminate(TaskCommon* ioPerformingTask);
};
}



#define CONTROL_INCLUDED

namespace pearlrt {

   class Control {
   public:
     typedef void(*InitFunction)(void);
     struct Initializer {
        InitFunction init;
        struct Initializer * next;
      };
   private:
      Control();
      static int exitCode;
      static Initializer* first;

   public:

      static void setExitCode(const Fixed<15> x);

      static int getExitCode();

      static int addInitializer(Initializer * addMe);

      static void initModules();
   };
}

#define SENDXML_INCLUDED
namespace pearlrt {

   class SendXML {
   public:

      SendXML();

      static void sendExitCode();
   };
}

#define STDIN_INCLUDED

#define INTERRUPTABLESYSTEMDATIONNB_INCLUDED


namespace pearlrt {

   class InterruptableSystemDationNB: public SystemDationNB {
      public:

         void suspend(TaskCommon* ioPerformingTask);

         void terminate(TaskCommon* ioPerformingTask);
   };
}




namespace pearlrt {


   class StdIn: public InterruptableSystemDationNB {
   private:

      Mutex mutex;

      int cap;

      bool inUse;

      int capacity;

      FILE* fp;
      static bool _isDefined;
   public:

      StdIn();

      int capabilities();

      StdIn* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);

      void translateNewLine(bool doNewLineTranslation);

      void abortRead();

   };

}

#define STDOUT_INCLUDED





namespace pearlrt {


   class StdOut: public InterruptableSystemDationNB {
   private:

      Mutex mutex;

      int cap;

      bool inUse;

      FILE* fp;
      static int declaredDations;
   public:

      StdOut();

      int capabilities();

      StdOut* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);


      void translateNewLine(bool doNewLineTranslation);
   };

}

#define STDERROR_INCLUDED





namespace pearlrt {


   class StdError: public InterruptableSystemDationNB {
   private:

      Mutex mutex;

      int cap;

      bool inUse;

      int capacity;

      FILE* fp;
   public:

      StdError();

      int capabilities();

      StdError* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);

      void translateNewLine(bool doNewLineTranslation);
   };

}

#define CONSOLE_INCLUDED









#define CONSOLECOMMON_INCLUDED
namespace pearlrt {

   class ConsoleCommon {
   private:
      Mutex mutex;
      PriorityQueue waitingForOutput;
      TaskCommon* waitingForInput;
      bool writerInProgress;
      SystemDationNB * systemIn;
      SystemDationNB * systemOut;
      TaskCommon*  lastAdressedTask;
      bool insertMode;
      size_t  nbrEnteredCharacters;
      size_t  cursorPosition;
      bool inputStarted;
      struct Wchar {
         char page;
         char ch;
      };
      union InputLine {
         struct Wchar wLine[82];
         char compressedLine[164];
      } inputLine;
      void compress();
      int getChar(void);
      void putChar(char ch);
      void putChar(Wchar ch);
      void putString(const Wchar* string);
      void putString(const char* string);
      void goLeft(int n);
      void goRight(int n);
      bool removeFromInputList(TaskCommon * t);
      bool removeFromOutputList(TaskCommon * t);
      bool treatCommand(char* line);
      const char* helpHelp();
      void printHelp();
      const char* helpPrli();
      void printPrli();
      struct Commands {
         const char* command;
         const char* (ConsoleCommon:: *help)();
         void (ConsoleCommon:: *doIt)();
      } commands[3];
   public:

      void startNextWriter();

      ConsoleCommon();

      void setSystemDations(SystemDationNB* in, SystemDationNB* out);

      TaskCommon* treatLine(char** inputBuffer, size_t * length);

      void registerWaitingTask(void * task, int direction);

      void triggerWaitingTask(void * task, int direction);

      void terminate(TaskCommon * ioPerformingTask);

      void suspend(TaskCommon * ioPerformingTask);
   };
}



#define FULLDUPLEXDATIONABORTNB_INCLUDED


namespace pearlrt {



   class FullDuplexDationAbortNB : public SystemDationNB {
   private:
      TaskCommon * readingTask;
      TaskCommon * writingTask;
   public:

      FullDuplexDationAbortNB(void);


      void setReaderTask(TaskCommon * rt);

      void setWriterTask(TaskCommon * wt);

      void terminate(TaskCommon* ioPerformingTask);

      virtual void terminateReader(TaskCommon* ioPerformingTask)=0;

      virtual void terminateWriter(TaskCommon* ioPerformingTask)=0;

      void suspend(TaskCommon* ioPerformingTask);

      virtual void suspendReader(TaskCommon* ioPerformingTask)=0;

      virtual void suspendWriter(TaskCommon* ioPerformingTask)=0;
   };

}

namespace pearlrt {


   class Console: public SystemDationNB {
   private:

      Mutex consoleMutex;
      Mutex consoleMutexIn;
      Mutex consoleMutexOut;

      int cap;

      bool inUse;

      int capacity;
      bool keepRunningFlag;
      static Console * instance;
      ConsoleCommon consoleCommon;
      StdIn stdIn;
      StdOut stdOut;
      struct termios oldTerminalSetting;
      TaskCommon* currentTask;
      char * bufferFromConsoleCommon;
      size_t lengthOfConsoleCommonBuffer;
      size_t deliveredCharacters;
   public:

      void consoleLoop();

      static Console* getInstance();

      static bool isDefined();

      Console();

      ~Console();

      int capabilities();

      Console* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);

      void translateNewLine(bool doNewLineTranslation);

      void registerWaitingTask(void * task, int direction);

      void triggerWaitingTask(void * task, int direction);

      bool allowMultipleIORequests();

      void openSubDations();
      void suspend(TaskCommon * ioPerformingTask);
      void terminate(TaskCommon * ioPerformingTask);
      void informIOOperationCompleted(Dation::DationParams dir);
   };

}

#define PIPE_INCLUDED







namespace pearlrt {


   class Pipe: public InterruptableSystemDationNB {
   public:

      class PipeFile : public InterruptableSystemDationNB {
      private:
         FILE * fp;
         Pipe * 	myPipe;
         int            cap;
      public:

         bool inUse;

         PipeFile(Pipe* disc);

         int capabilities();

         PipeFile* dationOpen(const RefCharacter * fileName, int openParams);

         void dationClose(int closeParams);

         void dationRead(void * destination, size_t size);

         void dationWrite(void * destination, size_t size);

         void dationUnGetChar(const char c);

         void translateNewLine(bool doNewLineTranslation);
      };
   private:

      Mutex mutex;

      int cap;

      int capacity;

      int usedCapacity;
      PipeFile**  object;

      static char * devicePath;
      FILE* defaultReader;
      bool removeFile;
   public:

      Pipe(const char* name, const int nbrOfFiles, const char* params = NULL);

      ~Pipe();

      int capabilities();

      PipeFile* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);

      void translateNewLine(bool doNewLineTranslation);
   };

}

#define PIPENBR_INCLUDED








namespace pearlrt {


   class PipeNBR: public InterruptableSystemDationNB {
   public:

      class PipeFile : public InterruptableSystemDationNB {
      private:
         FILE * fp;
         PipeNBR * 	myPipe;
         int            cap;
      public:

         bool inUse;

         PipeFile(PipeNBR* disc);

         int capabilities();

         PipeFile* dationOpen(const RefCharacter * fileName, int openParams);

         void dationClose(int closeParams);

         void dationRead(void * destination, size_t size);

         void dationWrite(void * destination, size_t size);

         void dationUnGetChar(const char c);

         void translateNewLine(bool doNewLineTranslation);
      };
   private:

      Mutex mutex;

      bool nonBlockingRead;

      int cap;

      int capacity;

      int usedCapacity;
      PipeFile**  object;

      char * devicePath;
      FILE* defaultReader;
      bool removeFile;
   public:

      PipeNBR(const char* name, const int nbrOfFiles, const char* params = NULL);

      ~PipeNBR();

      int capabilities();

      PipeFile* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);

      void translateNewLine(bool doNewLineTranslation);
   };

}

#define DISC_INCLUDED





namespace pearlrt {


   class Disc: public InterruptableSystemDationNB {
   public:

      class DiscFile : public InterruptableSystemDationNB {
      private:
         FILE * fp;
         Disc * 	myDisc;
	 int  cap;
      public:
         Character<256> completeFileName;
         RefCharacter   rcFn;

         bool inUse;

         DiscFile(Disc* disc);

         int capabilities();

         DiscFile* dationOpen(const RefCharacter * fileName, int openParams);

         void dationClose(int closeParams);

         void dationRead(void * destination, size_t size);

         void dationWrite(void * destination, size_t size);

         void dationSeek(const Fixed<31>&p, const int dationParam);

         void dationUnGetChar(const char c);

         Fixed<31> dationEof();

         void translateNewLine(bool doNewLineTranslation);
      };
   private:

      Mutex mutex;

      int cap;

      int capacity;

      int usedCapacity;
      DiscFile**  object;
      char * devicePath;
      size_t pathLength;
   public:

      Disc(const char* dev, const int nbrOfFiles);

      ~Disc();

      int capabilities();

      DiscFile* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);

      void translateNewLine(bool doNewLineTranslation);
   };

}

#define UNIXSIGNAL_INCLUDED


namespace pearlrt {


   class UnixSignal : public Interrupt {
   private:
      static UnixSignal *listOfUnixSignals;
      UnixSignal * next;
      int        sigNum;
      static int isSet;
      static UnixSignal * sig1;
      static UnixSignal * sig2;
      static UnixSignal * sig3;
      static UnixSignal * sig15;
      static UnixSignal * sig16;
      static UnixSignal * sig17;
      static void unixSignalHandler(int sig);
   public:

      UnixSignal(int sigNum);

      static void updateSigMask(sigset_t * sig);

      static bool treat(int sig);

      void devEnable();

      void devDisable();
   };
}

#define SOFTINT_INCLUDED


namespace pearlrt {



   class SoftInt : public Interrupt {
   private:
      static int isSet;
   public:

      SoftInt(int nbr);

      void devEnable();

      void devDisable();
   };

}



#define SAMPLEBASICDATION_H_INCLUDED





namespace pearlrt {

   class SampleBasicDation: public SystemDationB {
   private:
      int16_t echo;
      void internalDationOpen();
      void internalDationClose();
   public:

      SampleBasicDation();



      SystemDationB* dationOpen(const RefCharacter* idf = 0, int openParam = 0);

      void dationClose(int closeParam = 0);

      void dationRead(void * data, size_t size);

      void dationWrite(void * data, size_t size);

      int capabilities();
   };
}

#define I2CBUS_INCLUDED

#define I2PROVIDER_INCLUDED

namespace pearlrt {

   class I2CProvider {
   public:

      enum I2CMessageFlags {

         READ = 0x01,

         TENBIT = 0x10
      };

      struct I2CMessage {
         uint16_t addr;
         uint16_t flags;
         uint16_t len;
         uint8_t * data;
      };

      virtual int readData(int adr, int n, uint8_t * data) = 0;

      virtual int writeData(int adr, int n, uint8_t * data) = 0;

      virtual void rdwr(int n, I2CMessage* data) = 0;
   };
}

namespace pearlrt {


   class I2CBus : public I2CProvider {
   private:
      int i2c_file;
      Mutex mutex;

   public:

      I2CBus(const char * deviceName);

      int readData(int adr, int n, uint8_t * data);

      int writeData(int adr, int n, uint8_t * data);

      void rdwr(int n, I2CMessage* data);
   };

}

#define BME280_H_INCLUDED





namespace pearlrt {


   class BME280: public SystemDationB {
   private:



      int dev[20];
      uint8_t selectedSensors;
      int tMeasurement;
      void internalDationOpen();
      void internalDationClose();
      int8_t read_sensor_data_forced_mode();
   public:

      BME280(I2CProvider * provider, int addr,
             int tempOs, int pressOs, int humOs, int iir,
             int tSleep);

      BME280* dationOpen(const RefCharacter* idf = 0, int openParam = 0);

      void dationClose(int closeParam = 0);

      void dationRead(void * data, size_t size);

      void dationWrite(void * data, size_t size);

      int capabilities();
   };

}

#define PCA9685PROVIDER_H_INCLUDED





namespace pearlrt {


   class PCA9685 {
   private:
      int16_t addr;
      int16_t prescaler;
      I2CProvider * provider;
   public:

      PCA9685(I2CProvider * provider, int addr, int prescaler);

      void writeChannel(int channel, int offValue);
   };

}

#define PCA9685CHANNEL_H_INCLUDED





namespace pearlrt {


   class PCA9685Channel: public SystemDationB {
   private:
      int16_t channel;
      PCA9685 * provider;
      void internalDationOpen();
      void internalDationClose();
   public:

      PCA9685Channel(PCA9685 * provider, int channel);

      PCA9685Channel* dationOpen(const RefCharacter* idf = 0, int openParam = 0);

      void dationClose(int closeParam = 0);

      void dationRead(void * data, size_t size);

      void dationWrite(void * data, size_t size);

      int capabilities();
   };

}

#define LM75_H_INCLUDED





namespace pearlrt {


   class LM75: public SystemDationB {
   private:
      int16_t addr;
      I2CProvider * provider;
      void internalDationOpen();
      void internalDationClose();
   public:

      LM75(I2CProvider * provider, int addr);

      LM75* dationOpen(const RefCharacter * idf = 0, int openParam = 0);

      void dationClose(int closeParam = 0);

      void dationRead(void * data, size_t size);

      void dationWrite(void * data, size_t size);

      int capabilities();
   };

}

#define ADS1015SE_H_INCLUDED





namespace pearlrt {


   class ADS1015SE: public SystemDationB {
   private:
      int16_t addr;
      int handle;
      uint8_t channel, gain;
      I2CProvider * provider;
   public:

      ADS1015SE(I2CProvider * provider, int _addr, int ch, int g);

      ADS1015SE* dationOpen(const RefCharacter* idf = 0, int openParam = 0);

      void dationClose(int closeParam = 0);

      void dationRead(void * data, size_t size);

      void dationWrite(void * data, size_t size);

      int capabilities();
   };

}

#define PCF8574_H_INCLUDED





namespace pearlrt {


   class PCF8574In: public SystemDationB {
   private:
      int16_t addr;
      int handle;
      uint8_t mask, start;
   public:

      PCF8574In(I2CProvider * provider, int addr, int s, int w);

      PCF8574In* dationOpen(const RefCharacter* idf = 0, int openParam = 0);

      void dationClose(int closeParam = 0);

      void dationRead(void * data, size_t size);

      void dationWrite(void * data, size_t size);

      int capabilities();
   };

}

#define PCF8574OUT_H_INCLUDED





namespace pearlrt {


   class PCF8574Out: public SystemDationB {
   private:
      int16_t addr;
      int handle;
      uint8_t mask, start;
   public:

      PCF8574Out(I2CProvider * provider, int addr, int s, int w);

      PCF8574Out* dationOpen(const RefCharacter* idf = 0, int openParam = 0);

      void dationClose(int closeParam = 0);

      void dationRead(void * data, size_t size);

      void dationWrite(void * data, size_t size);

      int capabilities();
   };

}
 #pragma once

#define FLOATSWITCH_H_INCLUDED







namespace pearlrt {
   class FloatSwitch: public SystemDationB {
   public:
      FloatSwitch();
      ~FloatSwitch();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   };
}

#define PUMP_H_INCLUDED







namespace pearlrt {
  class Pump: public SystemDationB {
   public:
      Pump(int start);
      ~Pump();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   private:
      const int start;
  };
}

#define VALVE_H_INCLUDED







namespace pearlrt {
  class Valve: public SystemDationB {
   public:
      Valve();
      ~Valve();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   };
}

#define PRESSURESENSOR_H_INCLUDED







namespace pearlrt {
   class PressureSensor: public SystemDationB {
   private:
     const int addr;
   public:
      PressureSensor(int addr);
      ~PressureSensor();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   };
}
 #pragma once

#define SCREWMOTOR_H_INCLUDED







namespace pearlrt {
  class ScrewMotor: public SystemDationB {
   public:
      ScrewMotor(int start);
      ~ScrewMotor();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   private:
      const int start;
  };
}

#define TEMPERATURESENSOR_H_INCLUDED







namespace pearlrt {
   class TemperatureSensor: public SystemDationB {
   private:
     const int addr;
   public:
      TemperatureSensor(int addr);
      ~TemperatureSensor();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   };
}

#define FILAMENTDIAMETERSENSOR_H_INCLUDED







namespace pearlrt {
   class DiameterSensor: public SystemDationB {
   private:
     const int addr;
   public:
      DiameterSensor(int addr);
      ~DiameterSensor();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   };
}

#define SCREWHEATER_H_INCLUDED







namespace pearlrt {
  class ScrewHeater: public SystemDationB {
   public:
      ScrewHeater(int start);
      ~ScrewHeater();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   private:
      const int start;
  };
}

#define SPOOLERMOTOR_H_INCLUDED







namespace pearlrt {
  class SpoolerMotor: public SystemDationB {
   public:
      SpoolerMotor(int start);
      ~SpoolerMotor();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   private:
      const int start;
  };
}

#define CONTACTSWITCH_H_INCLUDED







namespace pearlrt {
   class ContactSwitch: public SystemDationB {
   public:
      ContactSwitch();
      ~ContactSwitch();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   };
}

#define MSGIO_H_INCLUDED







namespace pearlrt {
  class MsgIO: public SystemDationB {
   public:
      MsgIO(int start);
      ~MsgIO();
      SystemDationB* dationOpen(const RefCharacter * idf = 0, int openParam = 0);
      void dationClose(int closeParam = 0);
      void dationWrite(void * data, size_t size);
      void dationRead(void * data, size_t size);
      int capabilities();
   private:
      const int start;
  };
}

#define TCPIPSERVER_INCLUDED










namespace pearlrt {


   class TcpIpServer: public SystemDationNB {

   private:

      Mutex mutex;

	struct sockaddr_in server, client;
	int sock, fd;
	int port;
        bool translateNewLineFlag;
   public:

      TcpIpServer(const int port);



      int capabilities();

      TcpIpServer* dationOpen(const RefCharacter * fileName, int openParams);

      void dationClose(int closeParams);

      void dationRead(void * destination, size_t size);

      void dationWrite(void * destination, size_t size);

      void dationUnGetChar(const char c);

      void translateNewLine(bool doNewLineTranslation);
   };

}


#define DEADLOCKOPERATION_H


namespace pearlrt {
   class DeadlockOperation {
   public:
      const char* contextType = "";
      const char* contextIdentifier = "";
      const char* actionType = "";
      const char* codeFilename = "";
      int codeLineNumber = 0;
      const char* resourcesType = "";
      std::vector<const char*> resourceIdentifiers;
      std::vector<int> resourceValues;
      std::vector<const char*> executingTaskIdentifiers;
      bool reserveBoltOperationsPossible = false;
      const char* executingTaskIdentifier = "";
      static constexpr const char* RESOURCE_TYPE_SEMAPHORE = "SEMA";
      static constexpr const char* ACTION_TYPE_REQUEST = "REQUEST";
      static constexpr const char* ACTION_TYPE_RELEASE = "RELEASE";
      static constexpr const char* RESOURCE_TYPE_BOLT = "BOLT";
      static constexpr const char* ACTION_TYPE_ENTER = "ENTER";
      static constexpr const char* ACTION_TYPE_LEAVE = "LEAVE";
      static constexpr const char* ACTION_TYPE_RESERVE = "RESERVE";
      static constexpr const char* ACTION_TYPE_FREE = "FREE";
      static constexpr const char* CONTEXT_TYPE_TASK = "TASK";
      static constexpr const char* CONTEXT_TYPE_PROCEDURE = "PROCEDURE";
      static DeadlockOperation duy_registerSeaRequest(const char* contextTaskIdentifier, const char* resourceIdentifier, const int codeLineNumber = 0)
      {
         DeadlockOperation deadlockOperation;
         deadlockOperation.contextType = DeadlockOperation::CONTEXT_TYPE_TASK;
         deadlockOperation.contextIdentifier = contextTaskIdentifier;
         deadlockOperation.codeLineNumber = codeLineNumber;
         deadlockOperation.resourcesType = DeadlockOperation::RESOURCE_TYPE_SEMAPHORE;
         deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_REQUEST;
         deadlockOperation.addResourceIdentifier(resourceIdentifier);
         deadlockOperation.addExecutingTaskIdentifier(contextTaskIdentifier);
         return deadlockOperation;
      }
      static DeadlockOperation duy_registerSeaRelease(const char* contextTaskIdentifier, const char* resourceIdentifier, const int codeLineNumber = 0)
      {
         DeadlockOperation deadlockOperation;
         deadlockOperation.contextType = DeadlockOperation::CONTEXT_TYPE_TASK;
         deadlockOperation.contextIdentifier = contextTaskIdentifier;
         deadlockOperation.codeLineNumber = codeLineNumber;
         deadlockOperation.resourcesType = DeadlockOperation::RESOURCE_TYPE_SEMAPHORE;
         deadlockOperation.actionType = DeadlockOperation::ACTION_TYPE_RELEASE;
         deadlockOperation.addResourceIdentifier(resourceIdentifier);
         deadlockOperation.addExecutingTaskIdentifier(contextTaskIdentifier);
         return deadlockOperation;
      }
      void addResourceIdentifier(const char* resourceIdentifier);
      void addResourceIdentifierWithValue(const char* resourceIdentifier, int resourceValue);
      void addExecutingTaskIdentifier(const char* executingTaskIdentifier);
   };
   class DissolveDeadlockOperationCycleEntry
   {
   public:
      DeadlockOperation deadlockOperation;
      std::string executingTaskIdentifier;
      std::string releasedResourceIdentifier;
   };
}

#define PERFORMEDDEADLOCKOPERATION_H


using namespace std;
namespace pearlrt {
   class PerformedDeadlockOperation {
   public:
      bool initialized = false;
      DeadlockOperation deadlockOperation;
      string taskIdentifier;
      bool resourceAssignedToTask = false;
      unsigned long long callTimestamp = 0;
   };
}

#define RUNTIME_GRAPH_H







using namespace std;
namespace pearlrt {
   class Graph {
   private:
      static const int NODE_STATE_NOT_VISITED = 0;
      static const int NODE_STATE_FINISHED_VISITING = 1;
      static const int NODE_STATE_IS_BEING_VISITED = -1;
      vector<int>* graph;
      bool cycleDetected = false;
      stack<int> cycleDetectionPathStack;
      vector<string> cycleDetectionPath;
      int nodeCount = 0;
      vector<string> nodeIdentifiers;
      void performDFS(int currentNode, int visitedNodes[]);
      int graphNodeCount = 0;
   public:
      int getNodeId(const string& nodeIdentifier);
      string getNodeIdentifier(int nodeId);
      void addEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier);
      void removeEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier);
      vector<string> getCycle();
      void printGraph();
      string getFirstEdge(const string& nodeIdentifier);
      vector<string> getOutgoingEdges(const string& nodeIdentifier);
      vector<string> getIncomingEdges(const string& nodeIdentifier);
      int addNode(const string& nodeIdentifier);
      Graph(const int graphNodeCount)
      {
         graph = new vector<int>[graphNodeCount];
         this->graphNodeCount = graphNodeCount;
      }
   };
}

#define RESOURCEALLOCATIONGRAPH_H











namespace pearlrt {
   class ResourceAllocationGraph {
   private:
      Graph* graph = nullptr;
      map<string, PerformedDeadlockOperation> edgeMetadata;
      vector<string> taskIdentifiers;
      vector<PerformedDeadlockOperation> deadlockOperationTrace;
      vector<DeadlockOperation> registeredDeadlockOperations;
      void addGraphEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier, DeadlockOperation& deadlockOperation, const string& taskIdentifier, bool resourceAssignedToTask);
      void removeGraphEdge(const string& sourceNodeIdentifier, const string& targetNodeIdentifier);
      static string getTaskAssignedToResource(const char* resourceIdentifier, const char* resourceType, vector<PerformedDeadlockOperation>& deadlockOperations);
      PerformedDeadlockOperation getPerformedDeadlockOperation(const string& taskIdentifier, const string& resourceIdentifier, bool isAllocated);
      void requestResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible);
      void releaseResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible);
      static void printResourceAllocationGraphOperation(const string& string);
   public:
      void insertDeadlockOperationInTrace(DeadlockOperation& deadlockOperation, const string& taskIdentifier, bool resourceAssignedToTask);
      bool isDeadlocked();
      void print();
      void requestCountableResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible);
      void releaseCountableResource(const string& taskIdentifier, const string& resourceIdentifier, DeadlockOperation& deadlockOperation, int currentResourceValue, bool operationIsPossible);
      void registerDeadlockOperation(const DeadlockOperation& deadlockOperation);
      ResourceAllocationGraph(const int resourceAllocationGraphNodeCount)
      {
         graph = new Graph(resourceAllocationGraphNodeCount);
      }
   };
}

#define DYNAMICDEADLOCKDETECTION_H





using namespace std;
namespace pearlrt {
   class DynamicDeadlockDetection {
   private:
      DynamicDeadlockDetection() {}
      ResourceAllocationGraph* resourceAllocationGraph{};
      static int resourceAllocationGraphNodeCount;
      static DynamicDeadlockDetection* instance;
      static std::mutex instanceMutex;
   public:
      static constexpr const char* RELEASE_DATE = "18.02.2022";
      static constexpr const bool ACTIVATE_STD_OUT = false;
      static bool isEnabled() {
         return (resourceAllocationGraphNodeCount > 0);
      }
      static DynamicDeadlockDetection* getInstance()
      {
         if (!instance)
         {
            instance = new DynamicDeadlockDetection();
            instance->resourceAllocationGraph = new ResourceAllocationGraph(resourceAllocationGraphNodeCount);
         }
         return instance;
      }

      static bool configure(int resourceAllocationGraphNodeCount);
      static bool getDeadlockSituation();
      static void registerDeadlockOperation(const DeadlockOperation& deadlockOperation);
      static void printDeadlockOperations();
      static unsigned long long getCurrentTimestamp();
      static string formatTimestamp(unsigned long long timestamp, bool inMilliseconds = true);
      static string formatIdentifier(const string& identifier);
      static void performDeadlockOperation(DeadlockOperation& deadlockOperation);
   };
}
