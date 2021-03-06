#!/bin/bash
### get_files_from_csv_list_via_PTGL_API.sh -- Usage example for the PTGL API using bash and wget: get all files from a CSV list ###
### This script will downnload the GML files of all ALBE protein graphs listed in the second text file 
###
### Written by Tim Schaefer, http://rcmd.org/ts/
###
###
### This script takes the list of graphs to download from a CSV list of PDB chains (the file LIST_FILE configured below). The line format of the file should be:
###     <PDB_ID>, <CHAIN>, other, fields, if, needed, ...
### Here are some example lines from a real file (remove the '### ' at the start of each line, obvisouly)
### 1s44,A,Up and Down Barrel,beta
### 1s44,B,Up and Down Barrel,beta
### 4nw4,A,Up and Down Barrel,beta
### 4iaw,A,Up and Down Barrel,beta
### 4iaw,B,Up and Down Barrel,beta


##### settings -- adapt to your needs #####
LIST_FILE="all_beta.csv"        
PTGL_API_BASEPATH="http://ptgl.uni-frankfurt.de/api/index.php/"
GRAPHTYPE="albe"


##### end of settings. no need to mess with stuff below this line. #####

## gets a list of PDB chains, one per line. Each line contains an entry like '7tim,A,...' for chain A of PDB 7TIM.
## we extract the first two fields (PDB ID and chain), separated by ','
awk -F, '{print $1 $2}' $LIST_FILE > all_chains_in_many_lines.txt

## replace the new lines with spaces
ALL_CHAINS_IN_ONE_LINE=$(tr '\n' ' ' < all_chains_in_many_lines.txt)

for PDBCHAIN in $ALL_CHAINS_IN_ONE_LINE; do
  PDBID=${PDBCHAIN:0:4}        # extract PDB ID (characters 0-3, inclusive)
  PDBID="$(echo $PDBID | tr '[:upper:]' '[:lower:]')"        # optional, make PDBID lower case (already the case in example lines above, but you never know)
  CHAIN=${PDBCHAIN:4:1}        # extract CHAIN ID (the 5th character)  
  API_PATH_GML="${PTGL_API_BASEPATH}/pg/${PDBID}/${CHAIN}/${GRAPHTYPE}/gml"       # construct the API query, adds something like 'pg/7tim/A/albe/gml' to the API base path
  OUTPUT_FILE="${PDBID}_${CHAIN}_${GRAPHTYPE}_PG.gml"
  wget -O $OUTPUT_FILE $API_PATH_GML  
  if [ $? -ne 0 ]; then
      echo "Failed to downloaded GML file for PDB $PDBID chain $CHAIN."  
  elseif
      echo "Downloaded GML file for PDB $PDBID chain $CHAIN to local file '$OUTPUT_FILE'."
  fi
done

echo "All done, exiting."