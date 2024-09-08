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

Error messages from the IMC have no circumflex and no error line:

aa.prl:24:34: error: PUT:SKIP-format: no backward positioning allowed
aa.prl:26:34: error: PUT:SKIP-format: no backward positioning allowed
<ul>
<li>The expectations of the source aa.prl must be present in aa.exp
<li>each error message must fit to one expectation
<li>each expectation must be used by one (1) error message
<ul>

Error messages without a file location wre introduced for the IMC check for
the test 'no MAIN task defined'
These messages must be defined in an initial source file with (line,col)= (xx,-1)


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

#define LINELENGTH 1024
#define SOURCELEN 128
#define DEBUG 1

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

void clearLine(char * line) {
   int i; 
   for (i=0; i<LINELENGTH; i++) {
      line[i] = 0;
   }
}
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

/* returns 0, if it doesn't look like an error message
	   1, if it looks likje an error messages with filename and location
	   2, if it look's like a error message without a file location
*/
int looksLikeErrorMessage(char* line) {
   char * pos, *pos1;
   int n,l,c;

printf("looksLike... got<%s<\n" , line);
   pos = strstr(line,".prl:");
   if (pos == NULL) {
       printf("pos is NULL\n");
       // let's see if we start with 'error:', 'warning:' or 'note:'
       if (strstr(line,"error:") != NULL ||
           strstr(line,"warning:") != NULL ||
           strstr(line,"note:") != NULL) {
printf("type=2\n");
          return 2;  // looks like an error without location
       }
       return 0;
   }
   // file name may not contain spaces
   for (char *  i=line; i<pos; i++) {
       if (isspace(*i)) return 0;
   }
   n = sscanf(pos+4,":%d:%d",&l,&c);
   //printf("n=%d l=%d c=%d\n",n,l,c);
   if (n !=2) {
printf("type=0\n");
     return 0;
   }
printf("type=1\n");
   return 1;
}

/*
 return 0, if a complete error message from the compiler was detected
        1, if the next error message (file:line:col:...) was detected
           instead of ^ or error-line
	-1 unexpected input detected; error emitted
*/
int parseError(char * line) {
   char * pos, *pos1;

         printf("line >%s<\n",line);
   int kindOfErrorMessage = looksLikeErrorMessage(line);
#if DEBUG==1
   printf("parseError: kindOfErrorMessage = %d\n", kindOfErrorMessage);
#endif

   if (kindOfErrorMessage == 0) {
         fprintf(stderr,"unexpected line >%s<\n",line);
         return -1;
   } else if (kindOfErrorMessage == 1) { 
      // ok, we have an error message like "fname.prl:11:33:...."
      // no further checks for format needed
      pos = strstr(line,":");
      *(pos) = '\0';
      strcpy(currentMessage.source, line);
#if DEBUG == 1
      printf("source file:>%s< remaining >%s<\n",line,pos+1);
#endif
      pos1 = strstr(pos+1,":");
      *pos1 = '\0';
      currentMessage.line = atoi(pos+1);
      pos = strstr(pos1+1,":");
      *pos = '\0';
      currentMessage.col = atoi(pos1+1);
#if DEBUG == 1
   printf("***line =%d col=%d pos=%p\n",currentMessage.line, currentMessage.col,pos);
#endif
      strncpy(currentMessage.message,pos+2,strlen(pos+1));
      trimTrailingWhiteSpace(currentMessage.message);

#if DEBUG == 1
      printf("got message:\n");
      printf("file: %s line: %d  col: %d\n", currentMessage.source,
           currentMessage.line, currentMessage.col);
      printf("message: >%s<\n", currentMessage.message);
#endif

   // look at the next line, whether there is an error message or the next error message
      clearLine(line);
      pos = fgets(line,LINELENGTH,stdin);
#if DEBUG == 1
      printf("next line: >%s<  pos=%p\n",line,pos);
#endif

      if (pos) {
         // not eof
         if (looksLikeErrorMessage(line)) {
#if DEBUG == 1
             printf("next error instead of sourceline found\n");
#endif
              return 1;
      }
      strncpy(currentMessage.sourceLine, line, strlen(line)-1);
      } else {
          printf("info: eof in error messages reached\n");
          return 0;
      }

#if DEBUG == 1
      printf("sourceline: >%s<\n", currentMessage.sourceLine);
#endif

      // read circumflex line
      clearLine(line);
      pos = fgets(line,LINELENGTH,stdin);
      if (pos) {
//printf(line);
            currentMessage.circumflexPos = strstr(line,"^") - line + 1;
      } else {
             fprintf(stderr,"<eof> detected\n");
             return 0;
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
      } else if (kindOfErrorMessage == 2) {
#if DEBUG == 1
         printf("kindOfErrorMessage %d\n", kindOfErrorMessage);
#endif
         currentMessage.source[0] = '\0';
         currentMessage.line = -1;
         currentMessage.col = -1;
	 currentMessage.circumflexPos = -1;
         strncpy(currentMessage.message,line,LINELENGTH-1);
         trimTrailingWhiteSpace(currentMessage.message);
      }
      return 0;
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
   if (strlen(fileName) == 0) {
	printf("no filename in error message an message not loaded\n");
        return NULL;
   }

   if (strcmp(fileName+strlen(fileName)-4,".prl") != 0) {
      fprintf(stderr,"unknown source extension\n");
      exit(-1);
   }

#if DEBUG == 1
    printf("requested source file >%s<\n", fileName);
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
    expFile->first = NULL;
    expFile->next = firstExpectationFile;
    firstExpectationFile = expFile;

    do {
       clearLine(line);
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
       exp->next=expFile->first;
       expFile->first = exp;
#if DEBUG == 1
       printf("read expectation line=%d col=%d\n", exp->line, exp->col);
       printf("  msg >%s<\n", exp->message);
       printf("  next = %p head@ %p\n\n" ,exp->next,expFile->first);
#endif                

    } while (! feof(fdExp));

    fclose(fdExp);
    return (expFile);
}

static void searchExpectationAndMarkUsed( struct ExpectationFile * expFile) {
   struct Expectation *exp;
   int found = 0;
//printf("searchAndMark start\n");
   // first pass - search exact match (line,col, message)
#if DEBUG == 1
   printf("-------- dump expectations \n");
   for (exp=expFile->first; exp!= NULL && found == 0; exp=exp->next) {
     printf("exp@ %p l=%d,c=%d, next=%p, msg>%s<\n",
           exp, exp->line, exp->col, exp->next, exp->message); 
   }
printf("----------- \n\n");
#endif
   for (exp=expFile->first; exp!= NULL && found == 0; exp=exp->next) {
printf("check l=%d,c=%d msg=%s\n", currentMessage.line, currentMessage.col, currentMessage.message);
printf(" with l=%d,c=%d msg=%s\n", exp->line, exp->col, exp->message);
       if (exp->line > 0 && exp->col == -1 &&
	strcmp(exp->message,currentMessage.message) == 0) {
            exp->useCount ++;
	    printf("%s:%d:%d: expectation fulfilled\n", 
                expFile->source, exp->line,exp->col);
           found = 1;
       } else if (exp->line == currentMessage.line &&
           exp->col == currentMessage.col) {
//printf("%d.%d: fits\n", exp->line, exp->col);
//printf("Expected: %s\n",  exp->message);
//printf("Current: %s\n",   currentMessage.message);
         if (strcmp(exp->message,currentMessage.message) == 0) {
            exp->useCount ++;
	    printf("%s:%d:%d: expectation fulfilled\n", 
                expFile->source, exp->line,exp->col);
           found = 1;
         } else {
//printf("** message differs\n");
         }
//printf("exp now: %p next %p\n", exp, exp->next);
       } 
//printf("-----\n");
   }
//printf("searchAndMark @1\n");
  
   if (found) {
      return;
   }
//printf("searchAndMark @2\n");

   found = 0;
   // second pass - (line,col) must match 
   for (exp=expFile->first; exp!= NULL && found == 0; exp=exp->next) {
       if (exp->line == currentMessage.line &&
           exp->col == currentMessage.col) {
            fprintf(stderr,"%s:%d:%d: message differs\n\t   found: >%s<\n\texpected: >%s<\n",
                 currentMessage.source,currentMessage.line,
                 currentMessage.col, currentMessage.message, exp->message);
            exp->useCount ++;
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
//printf("searchAndMark @3\n");
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
   int nextLinePresent = 0;

   if (narg <2) {
      fprintf(stderr,"need at least 1 source-file(s)\n");
      exit(-1);
   }

   // read given expectation file(s)
   for (int i=1; i<narg;i++) {
       strcpy(currentMessage.source, argv[i]);
       readExpectationIfNotLoadedYet();
   }
    
   errorCount = 0;

   do {
     if (!nextLinePresent) {
printf("read line\n");
      clearLine(line);
      result = fgets(line,LINELENGTH,stdin);
     }
printf("result=%d\n line=>%s<\n",result,line);

      if (result) {
         if (line[strlen(line)-1] != '\n') {
            fprintf(stderr,"input buffer too small\n");
            exit(-1);
         }
         line[strlen(line)-1] = '\0';
printf("parseError>%s<\n",line);
         nextLinePresent = parseError(line);
printf("nextLinePreset=%d\n",nextLinePresent);
	if (currentMessage.source[0]!='\0') {
         expFile = readExpectationIfNotLoadedYet();
         searchExpectationAndMarkUsed(expFile);
	} else {
         searchExpectationAndMarkUsed(firstExpectationFile);
        }
       }
    } while (nextLinePresent == 1 || !feof(stdin));

//printf("@scan\n");
    scanExpectations();

    if (errorCount) {
       fprintf(stderr,"test failed: %d errors found\n", errorCount);
       exit(1);
    } else {
        printf("test passed: no error found\n");
    } 
    return 0;
}
