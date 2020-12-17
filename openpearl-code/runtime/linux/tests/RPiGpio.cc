/////////////////////////////////////////////////////////////////////////////
// PROLOGUE
/////////////////////////////////////////////////////////////////////////////
#include <PearlIncludes.h>

namespace pearlrt {
    extern int createSystemElements();
}

static int dummy = pearlrt::createSystemElements();

const char* filename = (char*) "RPiGpio.prl";


/////////////////////////////////////////////////////////////////////////////
// CONSTANT POOL
/////////////////////////////////////////////////////////////////////////////
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_25_5(25);
static /*const*/ pearlrt::Fixed<3>         CONST_FIXED_P_4_3(4);
static /*const*/ pearlrt::Fixed<5>         CONST_FIXED_P_27_5(27);
static /*const*/ pearlrt::Fixed<1>         CONST_FIXED_P_1_1(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_0_31(0);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_P_1_31(1);
static /*const*/ pearlrt::Fixed<31>         CONST_FIXED_N_1_31(-1);
static /*const*/ pearlrt::Fixed<0>         CONST_FIXED_P_0_0(0);
static /*const*/ pearlrt::Character<1>         CONST_CHARACTER_aebdc9ac_9f68_4d80_83e8_a1524119e52b("u");
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_1(0x1);
static /*const*/ pearlrt::BitString<1>         CONST_BITSTRING_2(0x0);
static /*const*/ pearlrt::Duration          CONST_DURATION_P_0_0_1_0(1,0,1);

/////////////////////////////////////////////////////////////////////////////
// TASK SPECIFIERS
/////////////////////////////////////////////////////////////////////////////
SPCTASK(_T1);

/////////////////////////////////////////////////////////////////////////////
// STRUCTURE FORWARD DECLARATIONS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// STRUCTURE DECLARATIONS
/////////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////////
// SYSTEM PART
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// PROBLEM PART
/////////////////////////////////////////////////////////////////////////////


/////////////////////////////////////////////////////////////////////////////
// DATION SPECIFICATIONS
/////////////////////////////////////////////////////////////////////////////
extern pearlrt::Device *d_biti;
static pearlrt::SystemDationB* _biti = static_cast<pearlrt::SystemDationB*>(d_biti); 

extern pearlrt::Device *d_bito;
static pearlrt::SystemDationB* _bito = static_cast<pearlrt::SystemDationB*>(d_bito); 



/////////////////////////////////////////////////////////////////////////////
// DATION DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
pearlrt::DationTS _taste(_biti, pearlrt::Dation::IN );


pearlrt::DationTS _led(_bito, pearlrt::Dation::OUT );



/////////////////////////////////////////////////////////////////////////////
// CONSTANT SEMAPHORE ARRAYS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// CONSTANT BOLT ARRAYS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// ARRAY DESCRIPTORS
/////////////////////////////////////////////////////////////////////////////

/////////////////////////////////////////////////////////////////////////////
// TASK DECLARATIONS
/////////////////////////////////////////////////////////////////////////////
DCLTASK(_T1, (pearlrt::Prio( (pearlrt::Fixed<15>)255)), ((pearlrt::BitString<1>)1)) {
        pearlrt::BitString<1>  _on(CONST_BITSTRING_1); 

        pearlrt::BitString<1>  _off(CONST_BITSTRING_2); 

        pearlrt::BitString<4>  _work; 


        me->setLocation(26, filename);
        {
            _taste.dationOpen(
                0
            , (pearlrt::Character<1>*) 0
            , (pearlrt::Fixed<31>*) 0
            );
        }


        me->setLocation(27, filename);
        {
            _led.dationOpen(
                0
            , (pearlrt::Character<1>*) 0
            , (pearlrt::Fixed<31>*) 0
            );
        }


        me->setLocation(28, filename);
        {
                while ( 1 )
                {

                     
                                   me->setLocation(30, filename);
                                   // send statement 
                                   {


                                      pearlrt::IODataEntry dataEntries[]  = {
                                        {
                                           .dataType={pearlrt::IODataEntry::BIT,4},
                                           .dataPtr={.outData=&_work},
                                           .param1={.numberOfElements = 1}
                                        }
                                      };

                                      pearlrt::IODataList dataList = {
                                        .nbrOfEntries=sizeof(dataEntries)/sizeof(dataEntries[0]),
                                        .entry=dataEntries};
                                   pearlrt::IOFormatList formatList = {
                                   .nbrOfEntries=0,
                                        .entry=NULL};

                                     _led.send(me, &dataList , &formatList);
                                   }


                                   me->setLocation(31, filename);
                                       me->resume( pearlrt::Task::AFTER,
                                             /* at     */  pearlrt::Clock(),
                                             /* after  */  CONST_DURATION_P_0_0_1_0,
                                   	  /* when   */  0
                                              );



                                   me->setLocation(32, filename);
                                   _work = _work.bitCshift(CONST_FIXED_P_1_1);


                }

        }


}



