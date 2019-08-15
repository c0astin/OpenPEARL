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

#include "ConsoleCommon.h"
#include "TaskMonitor.h"
#include "TaskList.h"

namespace pearlrt {

// input character codes
#define BS 0x08
#define ESC 27
#define BRACKETOPEN '['
#define RIGHT 67
#define LEFT  68
#define INSERT 50
#define DELETE 51
#define HOME   'H'    // ESC O H
#define END    'F'    // ESC O F
#define NL '\n'
#define BEL '\a'  // alarm code
#define SPACE ' '

  static bool treat_command(char * line) ;
   static char* task_state(enum Task::TaskState t);

   ConsoleCommon::ConsoleCommon() {
      insertMode = false;
      lastAdressedTask = NULL;
      waitingForInput = NULL;
   }

   void ConsoleCommon::setSystemDations(SystemDationNB* in,
                                        SystemDationNB* out) {
      systemIn = in;
      systemOut = out;
   }

   int ConsoleCommon::getChar(void) {
      char ch;
      systemIn->dationRead(&ch, 1);
      return (ch);
   }

   void ConsoleCommon::putChar(char ch) {
      systemOut->dationWrite(&ch, 1);
   }

   void ConsoleCommon::putString(const char* string) {
      systemOut->dationWrite((void*)string, strlen(string));
   }

   void ConsoleCommon::goRight(int n) {
      while (n--) {
         putChar(inputLine[cursorPosition]);
         cursorPosition++;
      }
   }

   void ConsoleCommon::goLeft(int n) {
      while (n--) {
         putChar(BS);
      }
   }

   TaskCommon* ConsoleCommon::treatLine(char** inputBuffer, size_t * length) {
      TaskCommon * t;
      static TaskCommon * lastTaskEntered = NULL;
      TaskCommon * previousTaskInList = NULL;
      int ch;
      size_t i;
      int cursor = 0;
      bool endOfLineReceived = false;

      nbrEnteredCharacters = 0;
      cursorPosition = 0;
      inputStarted = false;

      while (!endOfLineReceived) {
         cursor = 0;       // no cursor position code detected
         ch = getChar();   // wait passive until character is received
         inputStarted = true;
         // test for combined character
         if (ch == ESC) {
            ch = getChar();

            if (ch == BRACKETOPEN) {
               ch = getChar();

               if (ch == RIGHT) {
                  cursor = RIGHT;
               } else if (ch == LEFT) {
                  cursor = LEFT;
               } else if (ch == INSERT) {
                  insertMode = ! insertMode;
//printf("INSERTnmode = %d\n", insertMode);
                  ch = getChar(); // discard trailing ~ character
                  cursor = -1;  // discard ch
               } else if (ch == DELETE) {
                  ch = getChar(); // discard trailing ~ character
                  cursor = DELETE;
               } else {
                  // ignore all other
                  printf("ignore ESC [ %x\n", ch);

                  cursor = -1;  // discard ch
               }

//               printf("cursor control: %d ch=%x\n", cursor, ch);
            } else if (ch == 'O') { // ESC O sequences for HOME and END
               ch = getChar();

               if (ch == HOME) {
                  cursor = HOME;
//printf("HOME\n");
               } else if (ch == END) {
                  cursor = END;
//printf("END\n");
               } else {
                  // ignore all other
                  printf("ignore ESC O %x\n", ch);
                  cursor = -1;  // discard ch
               }
            }
         }

         switch (cursor) {
         case -1: // discard character
            continue;

         case 0: // normal input
            if (ch == NL) {
               putChar(ch);
               // always add NL at the end and the NIL also
               inputLine[nbrEnteredCharacters++] = ch;
               inputLine[nbrEnteredCharacters] = 0;
//     printf("end-of-record found cp=%zu nbr=%zu\n>%s<\n",
//             cursorPosition, nbrEnteredCharacters, inputLine);
               endOfLineReceived = true;
            } else if (ch == 0x07f) {
               // del to left
               if (cursorPosition > 0) {
                  for (i = cursorPosition; i < nbrEnteredCharacters; i++) {
                     inputLine[i - 1] = inputLine[i];
                  }

                  nbrEnteredCharacters--;
                  goLeft(1);
                  cursorPosition --;

                  for (i = cursorPosition; i < nbrEnteredCharacters; i++) {
                     putChar(inputLine[i]);
                     cursorPosition++;
                  }

                  putChar(SPACE);
                  goLeft(nbrEnteredCharacters - cursorPosition + 1);
               }
            } else if (ch >= ' ' && ch < 0x7f) {
               // reserve space for NL and NIL
               if (nbrEnteredCharacters < sizeof(inputLine) - 2) {
                  if (insertMode) {
                     for (i = nbrEnteredCharacters; i >= cursorPosition; i--) {
                        inputLine[i + 1] = inputLine[i];
                     }

                     inputLine[cursorPosition] = ch;
                     nbrEnteredCharacters++;
                     putChar(ch);
                     cursorPosition ++;

                     for (i = cursorPosition; i < nbrEnteredCharacters; i++) {
                        putChar(inputLine[i]);
                     }

                     goLeft(nbrEnteredCharacters - cursorPosition);
                  } else {
                     inputLine[cursorPosition] = ch;

                     if (cursorPosition == nbrEnteredCharacters) {
                        nbrEnteredCharacters++;
                     }

                     putChar(ch);
                     cursorPosition ++;
                  }
               } else {
                  putChar(BEL);
               }
            } else {
               printf("character %x found - not treated  yet\n", ch);
            }

            break;

         case DELETE: // del key --> delete from right
            if (cursorPosition < nbrEnteredCharacters) {
               nbrEnteredCharacters--;

               for (i = cursorPosition; i < nbrEnteredCharacters; i++) {
                  inputLine[i] = inputLine[i + 1];
               }

               for (i = cursorPosition; i < nbrEnteredCharacters; i++) {
                  putChar(inputLine[i]);
               }

               putChar(SPACE);
               goLeft(nbrEnteredCharacters - cursorPosition + 1);
            }

            break;

         case RIGHT: // position to right if possible
            if (cursorPosition < nbrEnteredCharacters) {
               goRight(1);
            }

            break;

         case LEFT: // position to left if possible

            // printf("cursor left  - not treated  yet\n");
            if (cursorPosition > 0) {
               goLeft(1);
               cursorPosition--;
            } else {
               putChar(BEL);
            }

            break;

         case HOME:  // position to first position
            goLeft(cursorPosition);
            cursorPosition = 0;
            break;

         case END:  // position after last entered position
            if (cursorPosition < nbrEnteredCharacters) {
               goRight(nbrEnteredCharacters - cursorPosition);
            }

            break;
         }

      }

      // input line ready
      //   preset return values with complete line
      //   this may be truncates if a taskname was detected
      *length = nbrEnteredCharacters;
      *inputBuffer = inputLine;
      inputStarted = false;

      // check if line starts with (new) task name
      if (inputLine[0] == ':') {
         // let's search the next colon
         for (i = 1; i < nbrEnteredCharacters; i++) {
            if (inputLine[i] == ':') {
               if (i > 2) {
                  // task name found
                  // update length information
                  * length -= i + 1;
                  // let's check if the task waits for input
                  // and pass the effecitive input to the input processing
                  // and quit this function
                  inputLine[i] = '\0';

                  // pointer to predecessor in list for easy
                  // removal operation
                  for (t = waitingForInput;
                        t != NULL; t = t->getNext()) {
printf("search target task %s\n", t->getName());
                     // ignore leading underscore in task name
                     if (strcmp(t->getName()+1, inputLine + 1) == 0) {
                        // found adressed task
printf("search target (%s), (%s)\n", t->getName()+1, inputLine+1);

                        // and set the return parameters
                        *inputBuffer = inputLine + i + 1;
                        lastTaskEntered = t;
                        break;
                     }
                  }

                  if (t == NULL) {
                     lastTaskEntered = NULL; // forget last valid task
                     putString("\n:???: not waiting\n");
                     // return the complete input line
                     *length = nbrEnteredCharacters;
                     inputLine[i] = ':'; //restore 2nd colon
//                     *inputBuffer = inputLine;
 //                    *length = nbrEnteredCharacters;
                     startNextWriter();
printf("@2\n");
                     // discard this input line for further processing
                     return NULL;
                  }
               }
            }
         }

         // no 2nd task name delimiter found
         // will be treated as if no colon is at the first position
      } else if (inputLine[0] == '/') {
          if (treat_command(inputLine)) {
             startNextWriter();
             // nothing to do -- command was treated
             return 0;
          }
      }


printf("lastTaskEntered = %p\n", lastTaskEntered);
printf("... %s\n", lastTaskEntered->getName());
      // remove this task from wait queue
      previousTaskInList = NULL;
      for (t = waitingForInput; t != NULL; t = t->getNext()) {
printf("remove target task %s\n", t->getName());
         if (lastTaskEntered == t) {
            if (previousTaskInList) {
               previousTaskInList->setNext(lastTaskEntered->getNext());
               t->setNext(NULL);
            } else {
               waitingForInput = lastTaskEntered->getNext();
            }
            break;
         }
      }

      startNextWriter();  
      return lastTaskEntered;
   }

   void ConsoleCommon::startNextWriter() {
      TaskCommon * nextWriter = waitingForOutput.getHead();
      if (nextWriter) {
          waitingForOutput.remove(nextWriter);
          nextWriter->unblock();
      }
   }

   void ConsoleCommon::registerWaitingTask(void * task, int direction) {
      TaskCommon * t = (TaskCommon*) task;
//printf("ConsoleCommon: registerWaiting: %p dir=%d\n", task, direction);
      if (direction == Dation::IN) {
         t->setNext(waitingForInput);
         waitingForInput = t;
      } else if (direction == Dation::OUT) {
         if (inputStarted || waitingForOutput.getHead()) {
            // queue not empty --> add task as waiter
            waitingForOutput.insert(t);
         } else {
            // let the task do its output
            t->unblock();
         }
      } else {
          Log::error("ConsoleCommon::registerWaitung: illegal direction=%d",
          direction);
         throw theInternalDationSignal;
      }
   }
  
   void ConsoleCommon::suspend(TaskCommon * ioPerformingTask) {
      printf("ConsoleCommon::suspend called\n");
      terminate(ioPerformingTask);
   }

   bool ConsoleCommon::removeFromInputList(TaskCommon* taskToRemove) {
       TaskCommon * previousTaskInList = NULL;
       TaskCommon * t;

       for (t = waitingForInput; t != NULL; t = t->getNext()) {
          if ( t == taskToRemove) {
              // remove this task from wait queue
              if (previousTaskInList) {
                 previousTaskInList->setNext(t->getNext());
                 t->setNext(NULL);
             } else {
                 waitingForInput = t->getNext();
             }
             return true;
          }
          previousTaskInList = t; 
      }
      return false;
   }

   bool ConsoleCommon::removeFromOutputList(TaskCommon* taskToRemove) {
      TaskCommon * next = waitingForOutput.getHead();
      while (next) {
          if (next == taskToRemove) {
               waitingForOutput.remove(next);
               return true;
          }
          next=waitingForOutput.getNext(next);
      }
      return false;
   }


   void ConsoleCommon::terminate(TaskCommon * ioPerformingTask) {
      // let's check wether the task is doing input or output
      if (removeFromInputList(ioPerformingTask)) {
printf("removed %s from input list\n", ioPerformingTask->getName());
      for (TaskCommon * t=waitingForInput; t != NULL; t=t->getNext()) {
       printf("still waiting: %s\n", t->getName());
     }
      } else if (removeFromOutputList(ioPerformingTask)) {
printf("removed %s from output list\n", ioPerformingTask->getName());
      } else {
         Log::error("ConsoleCommon: task %s was nether in input nor in output list", ioPerformingTask->getName());
         throw theInternalTaskSignal;
      }
   }

  static bool treat_command(char * line) {
      Task * t;
      int j, n;
      char line1[80], line2[80], line3[80], line4[80];
      char* detailedState[] = {line1, line2, line3, line4};

      if (strcasecmp(line, "/PRLI\n") == 0) {
         printf("Number of pending tasks: %d\n",
                TaskMonitor::Instance().getPendingTasks());

         for (int i = 0; i < TaskList::Instance().size();  i++) {
            t = TaskList::Instance().getTaskByIndex(i);
            printf("%-10.10s  %3d  %2d  %-20.20s (%s:%d)\n", t->getName(),
                   (t->getPrio()).x,
                   t->getIsMain(),
                   task_state(t->getTaskState()),
                   t->getLocationFile(), t->getLocationLine());
            n = t->detailedTaskState(detailedState);

            for (j = 0; j < n ; j++) {
               printf("\t%s\n", detailedState[j]);
            }
         }
         return true;
      }
      return false;
   }
   static char* task_state(enum Task::TaskState t) {
      switch (t) {
      case Task::TERMINATED:
         return ((char*)"TERMINATED");

      case Task::SUSPENDED:
         return ((char*)"SUSPENDED");

      case Task::RUNNING:
         return ((char*)"RUNNING");

      case Task::BLOCKED:
         return ((char*)"SEMA/BOLT/IO_BLOCKED");

      case Task::SUSPENDED_BLOCKED:
         return ((char*)"SEMA/BOLT/IO_SUSP_BLOCKED");

      default:
         return ((char*)"unknown state");
      }
   };

}
