/*
convert idf link command into Makefile version
* to create the input file
  * just invoke idf.py -v build in the OpenPEARLProject folder
  * copy the prelast (eq: [70/71] action into a text file and remove
    text before the g++ invocation and any pending text after the link command
* the input file contains archives in the current project and
   in the esp/esp-idf folders
* these archives must become copied into
    $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32/prj  - in short $1/prj
       if the filename starts with the current working directory
     and
    $(CONFIG_INSTALL_Target)/lib/OpenPEARLesp32/idf  - in short $1/idf 
      if the filename starts with the absolute path of the location of esp/esp-idf
* the -L option must become adopted according the new location
  * all -L options are created with absolute path
  * the esp-idf-path is already known by the 'pattern' parameter
  * the project related folders must contain 'runtime/esp32/OpenPEARLProject'
    we may take the remaining part relative the the target location
  * the paths are based on ~/esp/esp-idf/components/ 
    and .../runtime/esp32/OpenPEARLProject/build/esp-idf/
* the -T option lists linkter command files. They must become copied 
  from the current -L base
* the output files are
  * esp_idf_build.mk with definitions for make
  * cp.cmd which should be sourced during the make install target
* -Wl,--Map and -Wl,--cref become removed
*/

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#define MAXLENGTH 256
#define CP_CMD "cp.cmd"
#define INPUT_MK "esp_idf_build.mk"
#define PATH_PATTERN_IN_L_OPTION "/runtime/esp32/OpenPEARLProject/build/esp-idf/"

static   FILE * in;
static   FILE * cp;
static   FILE * esp_idf_build;
static   char* patternCommon;
static   char* currentWorkingDir;;
static   char* installPath;
static   int fail = 0;

int stringEndsWith(char * str, char * end) {
   if (strlen(str) >= strlen(end) ) {
      if (strcmp(str+strlen(str)-strlen(end),end) == 0) {
         return 1;
      }
   }
   return 0;
}

int stringStartsWith(char * str, char * end) {
   if (strlen(str) >= strlen(end) ) {
      if (strncmp(str,end, strlen(end)) == 0) {
         return 1;
      }
   }
   return 0;
}

void discardFilename(char * filename) {
//printf("dis: %s\n", filename);
   while(filename[strlen(filename)-1]  != '/') {
      filename[strlen(filename)-1]  = '\0';
   }
   filename[strlen(filename)-1]  = '\0';
//printf("-->: %s\n", filename);
}

void treatArchive(char * filename) {
    char destination[MAXLENGTH];
    char destPath[MAXLENGTH];

    if (stringStartsWith(filename,patternCommon) == 1) {
       sprintf(destination,
               "%s/idf/%s",
               installPath,
		filename+strlen(patternCommon)); 
       strcpy(destPath,destination);
       discardFilename(destPath);
       fprintf(cp,"mkdir -p %s\n", destPath);
       fprintf(cp,"cp %s %s\n", filename, destination);
       fprintf(esp_idf_build," %s \\\n",  destination);
    } else if (stringStartsWith(filename,"esp-idf/") ==1) {
       sprintf(destination, 
               "%s/prj/%s",
	       installPath,
               filename+strlen("esp-idf/"));
       strcpy(destPath,destination);
       discardFilename(destPath);
       fprintf(cp,"mkdir -p %s\n", destPath);
       fprintf(cp,"cp build/%s %s\n", filename, destination);
       fprintf(esp_idf_build," %s \\\n",  destination);
    } else {
      fprintf(stderr,"unexpected archive pattern: %s\n", filename);
      fail = 1;
    }
}


int main(int narg, char **argv) {
   int ok;
   char * src = argv[1];
   char item[MAXLENGTH];
   char currentLinkSource[MAXLENGTH];
   char currentLinkDestination[MAXLENGTH];
   char destPath[MAXLENGTH];
   int itemNbr = 0;
   int skipItems=0;
   enum {NOTFORLINKER,MINUS_L, MINUS_T, MINUS_U};
   int nextIsForLinker = NOTFORLINKER;
   int beforeMinusO = 1;

   if (narg == 5) {
      src = argv[1];
      patternCommon = argv[2];
      currentWorkingDir = argv[3];
      installPath = argv[4];
   } else {
      fprintf(stderr,"call with\n");
      fprintf(stderr,"    name of file with idf-build command (e.g. linker.txt)\n");
      fprintf(stderr,"    pattern for common archives (e.g. /home/.../component) \n");
      fprintf(stderr,"    the current working directory as absolute path \n");
      fprintf(stderr,"    install path for libraries (e.g. /usr/local/lib/OpenPEARLesp32/\n");
      exit(1);
   }
   printf("src: %s\n",src);
   printf("patternCommon: %s\n",patternCommon);
   printf("currentWorkingDir: %s\n",currentWorkingDir);
   printf("installPath: %s\n",installPath);
  
   if (!stringEndsWith(patternCommon,"/")) {
      fprintf(stderr,"pattern must end with '/'\n");
      exit(1);
   } 

   in = fopen(src,"r");
   if (in == NULL) {
      fprintf(stderr,"%s not found\n", src);
      exit(1);
   }
   cp = fopen(CP_CMD,"w");
   if (cp == NULL) {
      fprintf(stderr,"could not create %s\n", CP_CMD);
      exit(1);
   }
   esp_idf_build = fopen(INPUT_MK,"w");
   if (esp_idf_build == NULL) {
      fprintf(stderr,"could not create %s\n", INPUT_MK);
      exit(1);
   }

   while (!feof(in)) {
      do {
        ok = fscanf(in,"%s",item);
        if (ok != 1) {
          continue;
        }
      } while (skipItems-- > 0) ;

      itemNbr ++;
      if (itemNbr == 1) {
          fprintf(esp_idf_build,"CXX=%s\n\n", item);
          fprintf(esp_idf_build,"LINK_PARAMETERS_1=");
      } else {
          if(nextIsForLinker != NOTFORLINKER) {
            if (nextIsForLinker == MINUS_L) {
	       strcpy(currentLinkSource, item);
               if (stringStartsWith(item,patternCommon)) {
                  sprintf(currentLinkDestination,
                          "%s/idf/%s",
                          installPath, item+strlen(patternCommon));
//               } else if (stringStartsWith(item,currentWorkingDir)) {
//                  sprintf(currentLinkDestination,
//                          "$1/%s/idf/%s",
//                          installPath, item+strlen(patternCommon));
//
	       } else {
                  sprintf(currentLinkDestination,
                          "%s/prj/%s",
                          installPath,
			  strstr(item,PATH_PATTERN_IN_L_OPTION)+
                          strlen(PATH_PATTERN_IN_L_OPTION));
               }
//printf("src %s\n dest %s\n", currentLinkSource,currentLinkDestination);
              fprintf(esp_idf_build," -L %s\\\n", currentLinkDestination);
            } else if (nextIsForLinker == MINUS_T) {
//printf(" -T *****\n src %s\n dest %s\n", currentLinkSource,currentLinkDestination);
              fprintf(esp_idf_build," -T %s\\\n", item);
       	      sprintf(destPath,"%s/%s", currentLinkDestination,item);
              discardFilename(destPath);	
              fprintf(cp,"mkdir -p %s\n", destPath);
              if (stringStartsWith(currentLinkSource, currentWorkingDir)) {
                 fprintf(cp," cp $1/%s/%s %s/%s\n",
                     currentLinkSource+strlen(currentWorkingDir),item,
                     currentLinkDestination, item);
              } else {
                 fprintf(cp," cp %s/%s %s/%s\n",
                     currentLinkSource,item,currentLinkDestination, item);
              }
            } else {
              fprintf(esp_idf_build,"%s\\\n", item);
            }
            nextIsForLinker = NOTFORLINKER;
          } else if (strcmp(item,"-o") == 0) {
             skipItems = 1;
             beforeMinusO = 0;
             fprintf(esp_idf_build,"\nLINK_PARAMETERS_2=");
          } else if (strcmp(item, "-u") == 0) {
            fprintf(esp_idf_build," -u ");
            nextIsForLinker = MINUS_U;
          } else if (strcmp(item, "-L") == 0) {
            nextIsForLinker = MINUS_L;
          } else if (strcmp(item, "-T") == 0) {
            nextIsForLinker = MINUS_T;
          } else if (stringStartsWith(item,"-Wl,--cref")) {
            // remove create cross reference
            fprintf(stderr,"info: ignoring %s\n", item);
          } else if (stringStartsWith(item,"-Wl,--Map")) {
            // remove mapfile setting
            fprintf(stderr,"info: ignoring %s\n", item);
          } else if (item[0] == '-') {
            fprintf(esp_idf_build," %s\\\n", item);
          } else {
             // must archive  should end with '.a'
             if (stringEndsWith(item,".a")) {
               treatArchive(item);
             } else {
               if (beforeMinusO) {
                   fprintf(stderr,"info: ignoring %s\n", item);
               } else {
                  fprintf(stderr,"unexpected item %s\n", item);
                  fail = 1;
               }
             }
          }
      }
   }
   fclose(in);
   return (fail);
}
