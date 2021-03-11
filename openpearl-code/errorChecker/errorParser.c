/**
\file 

Error parser reads error messages and compares them with expectations

E.g. the error output from the compiler looks like this

aa.prl:24:34: error: PUT:SKIP-format: no backward positioning allowed
  PUT 'hallo' TO stdout BY A,F(3),SKIP(-1);
                                  ^
aa.prl:28:27: error: PUT:X-format: no backward positioning allowed
  PUT 'hallo' TO stdout BY X(-1),SKIP, F(3),SKIP(-1);
                           ^
<ul>
<li>The expectations of the source aa.prl must be present in aa.exp
<li>each error message must fit to one expectation
<li>each expectation must be used by one (1) error message
<ul>

Principle of operation:
<ol>
<li>parse stdin for error messages
<li>derive source file, line,col and message from stdin
<li>read complained source code line from stdin
<li>read circumflex line from stdin
<li>if all reading was succeccful we have a wellformed message
<li>read expectations if they are not loaded yet
<li>compare error information with expectations and mark used expectations
<li>the sequence of messages and expectations is irrelevant
</ol>

Error treatment
<ul>
<li>emit errors about not wellformed messages
<li>emit errors about unexpected messages
<li>emit errors about not fulfilled expectations
</ul>
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <ctype.h>

#define LINELENGTH 256
#define SOURCELEN 128
#define DEBUG 0

static int errorCount;

struct Expectation {
   struct Expectation * next;
   int line;
   int col;
   int useCount;
   char message[LINELENGTH];
};
 
struct ExpectationFile {
   char source[SOURCELEN];
   struct ExpectationFile * next;
   struct Expectation * first;
} * firstExpectationFile = NULL;
 
static struct Message {
   char source[SOURCELEN];
   int line;
   int col;
   char message[LINELENGTH];
   char sourceLine[LINELENGTH];
   int circumflexPos;
} currentMessage;

void trimTrailingWhiteSpace(char * line) {
   int last;
   do {
       last = strlen(line)-1;
       if (!isspace(line[last])) {
         break;
       }
       line[last] = '\0';
   } while(strlen(line)>0);
}

void parseError(char * line) {
   char * pos, *pos1;
   pos = strstr(line,":");
   *pos = '\0';
   strcpy(currentMessage.source, line);
   pos1 = strstr(pos+1,":");
   *pos1 = '\0';
   currentMessage.line = atoi(pos+1);
   pos = strstr(pos1+1,":");
   *pos = '\0';
   currentMessage.col = atoi(pos1+1);
   strncpy(currentMessage.message,pos+2,strlen(pos+1)-1);
   currentMessage.message[strlen(pos+1)-2] = '\0';
   trimTrailingWhiteSpace(currentMessage.message);

#if DEBUG == 1
   printf("got message:\n");
   printf("file: %s line: %d  col: %d\n", currentMessage.source,
       currentMessage.line, currentMessage.col);
   printf("message: >%s<\n", currentMessage.message);
#endif

   pos = fgets(line,LINELENGTH,stdin);
   if (pos) {
      strncpy(currentMessage.sourceLine, line, strlen(line)-1);
   } else {
       fprintf(stderr,"malformed message\n");
       exit(-1);
   }

#if DEBUG == 1
   printf("sourceline: >%s<\n", currentMessage.sourceLine);
#endif

   pos = fgets(line,LINELENGTH,stdin);
   if (pos) {
printf(line);
      currentMessage.circumflexPos = strstr(line,"^") - line + 1;
   } else {
       fprintf(stderr,"malformed message\n");
       exit(-1);
   }
#if DEBUG == 1
   printf("circumflex at: %d\n", currentMessage.circumflexPos);
#endif
   if (currentMessage.circumflexPos != currentMessage.col) {
      fprintf(stderr,"%s:%d:%d: circumflex position (%d) differs from column\n",
                currentMessage.source, currentMessage.line,
                currentMessage.col, currentMessage.circumflexPos);
      errorCount ++;
   }
}

static struct ExpectationFile* readExpectationIfNotLoadedYet() {
   char fileName[SOURCELEN];
   struct ExpectationFile * expFile;
   struct Expectation *exp;
   FILE * fdExp;
   char line[LINELENGTH+20];
   char * pos1, *pos2;

   strcpy(fileName,currentMessage.source);

   for (expFile=firstExpectationFile; expFile != NULL;
        expFile = expFile->next) {
       if (strcmp(expFile->source,fileName) == 0) {
            return(expFile);
       }
   } 

   if (strcmp(fileName+strlen(fileName)-4,".prl") != 0) {
      fprintf(stderr,"unknown source extension\n");
      exit(-1);
   }

#if DEBUG == 1
    printf("requsted source file >%s<\n", fileName);
#endif 


    expFile = malloc(sizeof(*expFile));
    if (expFile == NULL) {
        fprintf(stderr,"malloc failed -- no more memory");
        exit(1);
    }
    strcpy(expFile->source,fileName);

    strcpy(fileName+strlen(fileName)-4,".exp");
    fdExp = fopen(fileName,"r");
    if (fdExp == NULL) {
        fprintf(stderr,"could not open %s (%s)\n", fileName, strerror(errno));
        exit(1);
    }
    expFile->next = firstExpectationFile;
    firstExpectationFile = expFile;

    do {
       pos1 = fgets(line, sizeof(line), fdExp);
       if (pos1==NULL && !feof(fdExp)) {
           fprintf(stderr,"read error in %s\n", fileName);
           exit(-1);
       }
       if (pos1 == NULL) {
          break;
       } 
       trimTrailingWhiteSpace(line);

       exp = malloc(sizeof(*exp));
       exp->next = NULL;
       exp->useCount = 0;
   
       pos2 = strstr(pos1,",");

       *pos2 = '\0';
       exp->line = atoi(pos1);

       pos1 = strstr(pos2+1,",");
       *pos1 = '\0';
       exp->col = atoi(pos2+1);
       
       strcpy(exp->message,pos1+1);
#if DEBUG == 1
       printf("read expectation line=%d col=%d\n", exp->line, exp->col);
       printf("  msg >%s<\n", exp->message);
#endif                
       exp->next=expFile->first;
       expFile->first = exp;

    } while (! feof(fdExp));

    fclose(fdExp);
    return (expFile);
}

static void searchExpectationAndMarkUsed( struct ExpectationFile * expFile) {
   struct Expectation *exp;
   int found = 0;

   // first pass - search exact match (line,col, message)
   for (exp=expFile->first; exp!= NULL && found == 0; exp=exp->next) {
       if (exp->line == currentMessage.line &&
           exp->col == currentMessage.col) {
         if (strcmp(exp->message,currentMessage.message) == 0) {
            exp->useCount ++;
	    printf("%s:%d:%d: expectation fulfilled\n", 
                expFile->source, exp->line,exp->col);
           found = 1;
         }
       } 
   }
  
   if (found) {
      return;
   }

   found = 0;
   // second pass - (line,col) must match 
   for (exp=expFile->first; exp!= NULL && found == 0; exp=exp->next) {
       if (exp->line == currentMessage.line &&
           exp->col == currentMessage.col) {
            fprintf(stderr,"%s:%d:%d: message differs\n\t>%s<\n\t>%s<\n",
                 currentMessage.source,currentMessage.line,
                 currentMessage.col, currentMessage.message, exp->message);
            errorCount ++;
            found = 1;
       } 
   }
   if (! found) {
            fprintf(stderr,"%s:%d:%d: message not expected\n\t%s\n",
                 currentMessage.source,currentMessage.line,
                 currentMessage.col, currentMessage.message);
             errorCount ++;
   }
}

static void scanExpectations() {
   struct ExpectationFile * expFile;
   struct Expectation *exp;

   for (expFile=firstExpectationFile;
        expFile != NULL;
        expFile = expFile->next) {

      for (exp=expFile->first; exp!= NULL; exp=exp->next) {
         if (exp->useCount == 0) {
	    fprintf(stderr,"%s:%d:%d: expectation never met\n\t%s\n",
                expFile->source, exp->line,exp->col, exp->message);
            errorCount ++;
         }
         if (exp->useCount > 1) {
	    fprintf(stderr,"%s:%d:%d: expectation  met %d times\n\t%s\n",
                expFile->source, exp->line,exp->col, 
                exp->useCount,exp->message);
            errorCount ++;
         }
      }
   } 
}

int main(int narg, char*argv[]) {
   char line[LINELENGTH];
   char * result;
   struct ExpectationFile * expFile;

   if (narg <2) {
      fprintf(stderr,"need at least 1 source-file(s)\n");
      exit(-1);
   }

   // read passed expectation file(s)
   for (int i=1; i<narg;i++) {
       strcpy(currentMessage.source, argv[i]);
       readExpectationIfNotLoadedYet();
   }
    
   errorCount = 0;

   do {
      result = fgets(line,LINELENGTH,stdin);
      if (result) {
         if (line[strlen(line)-1] != '\n') {
            fprintf(stderr,"input buffer too small\n");
            exit(-1);
         }
         parseError(line);
         expFile = readExpectationIfNotLoadedYet();
         searchExpectationAndMarkUsed(expFile);
       }
    } while (!feof(stdin));

    scanExpectations();

    if (errorCount) {
       fprintf(stderr,"test failed: %d errors found\n", errorCount);
       exit(1);
    } else {
        printf("test passed: no error found\n");
    } 
    return 0;
}
