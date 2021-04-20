/**
\file

Generate a csv-file for all annotations in a source file

The annotations are placed at in a multiline comment with a start pattern
'/*$' at the beginning of a line.
The location of the error line is derived from the read lines in the
input file.
The column position is derived from a circumflex character ether in the  
start of the annotation - or in the next line.
If no circumflex is used, the column position may be defined with
'col=xxx' in the start of the annotation line.
After the circumflex character is detected the next line denotes the message
text.

Each source files is treated individually.
The output is named like the source file, with the extension '.exp'
*/

#include <string.h>
#include <stdlib.h>
#include <stdio.h>
#include <errno.h>

#include "config.h"

static void removeSpaces(char * line) {
   char * help=line;
   do {
      while(*help == ' ') {
         help ++;
      }
   } while (*line++ = *help++);
}

static void printHelp(char * fn) {
   fprintf(stderr,"Usage: %s file\n", fn);
   fprintf(stderr,"       %s -h\n", fn);
   fprintf(stderr,"    file must have the extension .prl\n"); 
   fprintf(stderr,"    -h   prints this message\n"); 
}


int main(int narg, char * argv[]) {
   char * inputFileName;
   char outputFileName[128];
   FILE * input, *output;
   int lineNumber;
 
   // needed if more messages occur on one input line
   int lastRealInputLineNumber;
   int annotationCount;
   int annotationStatus;
   char inputLine[LINELENGTH];
   int ok;
   char * result;

   struct Annotation {
       int line;
       int col;
       char message[LINELENGTH];
   } annotation;

   if (narg != 2) {
      printHelp(argv[0]);
      exit(-1);
   }
   if (strcmp(argv[1],"-h")== 0) {
      printHelp(argv[0]);
      exit(0);
   }

   inputFileName = argv[1];
   ok = 1;
   if (strlen(inputFileName) < 4) {
      ok = 0;
   } else {
      if (strcmp(inputFileName+strlen(inputFileName)-4,".prl") != 0) {
         ok = 0;
      }
   }
   if (!ok) {
      fprintf(stderr,"%s: need extension .prl\n", inputFileName);
      exit(-1);
   }

   if (strlen(inputFileName) > sizeof(outputFileName)-1) {
      fprintf(stderr,"%s: input file name exceeds length of %u\n",
              inputFileName, sizeof(outputFileName)-1);
      exit(-1);
   }

   input = fopen(inputFileName,"r");
   if (input == NULL) {
       fprintf(stderr,"%s: could not open  (%s)\n", 
               inputFileName,strerror(errno));
       exit(-1);
   }
   strcpy(outputFileName, inputFileName);
   outputFileName[strlen(inputFileName)-4] = '\0';
   strcat(outputFileName,".exp");
//for (int i=0; i<strlen(outputFileName); i++) {
//   printf("%02X (%c) ", outputFileName[i],outputFileName[i]);
//}
//printf("\n");
   output = fopen(outputFileName,"w");
   if (output == NULL) {
      fprintf(stderr,"%s: could not create outfile file (%s)\n",
	   inputFileName,strerror(errno));
      exit(-1);
   }

   // copy input to output untile next annotation is reached
   lineNumber = 0;
   lastRealInputLineNumber = -1;
   annotationCount = 0;
   annotationStatus = 0;

   do {
      lineNumber ++;
      result = fgets(inputLine, sizeof(inputLine), input); 
      if (result) {
         if (result[strlen(result)-1] != '\n' && ! feof(input)) {
            fprintf(stderr,"%s:%d: line exceeds length limit\n",
		inputFileName,lineNumber);
            exit(-1);
         } 
         if (result[strlen(result)-1] != '\n' && feof(input)) {
            // last line without newline is ok
         }
//printf("new line %d >%s<", lineNumber,inputLine);
         switch (annotationStatus) {
            case 0: // not inside of annotation
                 if (strncmp(inputLine,"/*$",3) == 0) {
                    // start of annotation found
                    if (lastRealInputLineNumber < 0) {
                       fprintf(stderr,"%s:%d: need at least one source line before annotation\n",
                              inputFileName, lineNumber);
                       exit(-1);
                    }
//printf("line %d: annotation start\n", lineNumber);
                    annotation.line = lastRealInputLineNumber; //lineNumber-1;
                     
                    annotationStatus = 1;
                 
                    if (strstr(inputLine,"*/")) {
                       fprintf(stderr,"%s:%d: malformed annotation\n",
                              inputFileName, lineNumber);
                       exit(-1);
                    } 
                    if (strchr(inputLine,'^')) {
                        annotation.col = strchr(inputLine,'^') - inputLine+1;
                        annotationStatus = 2;
                    } else if (strstr(inputLine,"col=")) {
                        annotation.col = atoi(strstr(inputLine,"col=")+4);
                        annotationStatus = 2;
printf("col=%d\n", annotation.col);

                    }
                 } else {
//printf("update lastRealLineNumber from %d to %d\n", lastRealInputLineNumber, lineNumber);
                    lastRealInputLineNumber = lineNumber;
                 }
                 break;
            case 1: // expect ^ in this line 
                 if (strstr(inputLine,"*/")) {
                    fprintf(stderr,"%s:%d: malformed annotation\n",
                              inputFileName, lineNumber);
                    exit(-1);
                 } 
                 if (strchr(inputLine,'^') == NULL) {
                    fprintf(stderr,"%s:%d: no '^' found in annotation\n",
                              inputFileName, lineNumber);
                     exit(-1);
                 }
                 annotation.col = strchr(inputLine,'^') - inputLine+1;
                 annotationStatus = 2;
                 break;
             case 2: // expect message
                 if (strstr(inputLine,"*/")) {
                    fprintf(stderr,"%s:%d: malformed annotation\n",
                              inputFileName, lineNumber);
                    exit(-1);
                 } 
                 strcpy(annotation.message, inputLine);
                 annotationStatus = 3;
                 break;
             case 3: // expect closing comment
                 if (strstr(inputLine,"*/") == NULL) {
                    fprintf(stderr,"line %d: malformed annotation\n",
                            lineNumber);
                    exit(-1);
		 } 
                 fprintf(output,"%d,%d,%s", annotation.line,
                          annotation.col, annotation.message);
                 annotationCount ++;
                 annotationStatus = 0;
                 break; 
         }
         //fputs(inputLine, output); 
      } 
   } while (!feof(input));
   fclose(input);
   fclose(output);

   if (annotationCount ==0) {
      fprintf(stderr,"%s:%d: no annotation found\n",
             inputFileName);
      return 1;
   } else {
      printf("%s: %d annotations found\n", inputFileName,annotationCount);
   }
   return 0; 
}
