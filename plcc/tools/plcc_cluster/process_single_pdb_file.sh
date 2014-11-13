#!/bin/bash
## process_single_pdb_file.sh -- processes a single PDB file. This is used by the MPI version of the update scripts.
##                               You can also use it on its own, of course.
##
## Written by ts_2011

APPTAG="[PROC_1PDB]"
CFG_FILE="settings_statistics.cfg"




#echo "$APPTAG Using settings from file '$CFG_FILE'."



## get settings
source $CFG_FILE

ERROR_LOG="/dev/stderr"
ERROR_LOG_GET_PDB_FILE="/dev/stderr"		# may be reset below once PDB ID is known
ERROR_LOG_CREATE_DSSP_FILE="/dev/stderr"	# may be reset below once PDB ID is known
SILENT="YES"

## IMPORTANT: set this to "YES" if plcc is run with '-k' / '--output-subdir-tree'
PLCC_RUNS_IN_SUBDIR_TREE_MODE="YES"

################################################## functions ##################################################

if [ "$SILENT" = "NO" ]; then
  echo "$APPTAG ================ Processing single PDB file '$1' ==================="
fi

function del_output()
{
    ## remove various output files
    L_PDBID="$1"	# first parameter to this function
    list_size=0
    num_del=0
    for f in $L_PDBID.pdb $L_PDBID.dssp $L_PDBID.geolig $L_PDBID.ligands $L_PDBID.contactstats $L_PDBID.models $L_PDBID.dssplig $L_PDBID.geolig $L_PDBID.pymol $L_PDBID.geo
    do
    	let list_size++
    	if [ -w $f ]; then
    		rm -f $f
    		#echo "$APPTAG   Deleted file $f."
    		let num_del++
    	fi
    done


    ## e.g. 'pdb3kmf.ent.gz'
    RAWPDBFILENAME="${PDBFILE_DL_PREFIX}${L_PDBID}${PDBFILE_DL_SUFFIX}"
    if [ -w $RAWPDBFILENAME ]; then
	rm -f $RAWPDBFILENAME
    fi

    #echo "$APPTAG Checked for $list_size files, deleted $num_del."
}

function report_and_exit()
{
    L_EXITCODE="$1"	# first parameter to this function
    TIME_END=$(date)

    #echo "$APPTAG Done, handled $NUM_HANDLED of the $NUM_FILES files ($NUM_SUCCESS ok, $NUM_TOTAL_FAIL failed)."
    echo "$APPTAG Done, handled $NUM_HANDLED of the $NUM_FILES files ($NUM_SUCCESS ok, $NUM_TOTAL_FAIL failed)." >>$DBINSERT_LOG
    
    #echo "$APPTAG Started at '$TIME_START', finished at '$TIME_END'."
    echo "$APPTAG Started at '$TIME_START', finished at '$TIME_END'." >>$DBINSERT_LOG    

    #echo "$APPTAG All done, exiting."
    echo "$APPTAG All done, exiting." >>$DBINSERT_LOG
    exit $L_EXITCODE
}

function report_and_exit_nolog()
{
    L_EXITCODE="$1"	# first parameter to this function
    TIME_END=$(date)

    #echo "$APPTAG Done, handled $NUM_HANDLED of the $NUM_FILES files ($NUM_SUCCESS ok, $NUM_TOTAL_FAIL failed)."
    #echo "$APPTAG Started at '$TIME_START', finished at '$TIME_END'."    

    #echo "$APPTAG All done, exiting."
    exit $L_EXITCODE
}

## deletes a file unless it is a device, i.e., the name starts with /dev/
function delete_file_unless_dev()
{
    FILENAME="$1"	# first parameter to this function
    if [[ FILENAME == /dev/* ]] ;
    then
      rm -f $FILENAME
    fi
}


################################################## end of functions ##################################################



############################################   check various stuff  ############################################
## Checking all of this definitely pays off because we can stop now instead of getting the same error message for
##  tens of thousands of PDB files in the loop below...

## make sure the settings have been read
if [ -z "$SETTINGSREAD" ]; then
    echo "$APPTAG ##### ERROR: Could not load settings from file '$CFG_FILE'."
    echo "$APPTAG ##### ERROR: Could not load settings from file '$CFG_FILE'." >>$ERROR_LOG
    exit 1
fi


## check for status dir
if [ ! -d $STATUSDIR ]; then
    echo "$APPTAG ##### ERROR: Status dir '$STATUSDIR' does not exist but is required. Create it. Exiting."
    echo "$APPTAG ##### ERROR: Status dir '$STATUSDIR' does not exist but is required. Create it. Exiting." >>$ERROR_LOG
    exit 1
fi


## check for log dir
if [ ! -d $LOGDIR ]; then
    echo "$APPTAG ##### ERROR: Log dir '$LOGDIR' does not exist but is required. Create it. Exiting."
    echo "$APPTAG ##### ERROR: Log dir '$LOGDIR' does not exist but is required. Create it. Exiting." >>$ERROR_LOG
    exit 1
fi


## make sure no rsync update is currently running
if [ -f "$LOCKFILE_RSYNC" ]; then
    echo "$APPTAG ##### ERROR: Rsync update currently in progress it seems, exiting."
    echo "$APPTAG ##### ERROR: Rsync update currently in progress it seems, exiting." >>$ERROR_LOG
    echo "$APPTAG Delete '$LOCKFILE_RSYNC' and try again if you are sure this is not the case."
    exit 1
fi


## make sure no database update is currently running
#if [ -f "$LOCKFILE_DBINSERT" ]; then
#    echo "$APPTAG ##### ERROR: Database update already in progress it seems, exiting. #####"
#    echo "$APPTAG Delete '$LOCKFILE_DBINSERT' and try again if you are sure this is not the case."
#    exit 1
#fi


## check plcc_run dir
if [ ! -d $PLCC_RUN_DIR ]; then
    echo "$APPTAG ##### ERROR: The plcc_run directory '$PLCC_RUN_DIR' does not exist. Check settings."
    echo "$APPTAG ##### ERROR: The plcc_run directory '$PLCC_RUN_DIR' does not exist. Check settings." >>$ERROR_LOG
    exit 1
fi


## check update dir
if [ ! -d $UPDATEDIR ]; then
    echo "$APPTAG ##### ERROR: The update directory '$UPDATEDIR' does not exist. Check settings."
    echo "$APPTAG ##### ERROR: The update directory '$UPDATEDIR' does not exist. Check settings." >>$ERROR_LOG
    exit 1
fi

GET_PDB_FILE_SCRIPT="./get_pdb_file.sh"
## check pdb script
if [ ! -x $PLCC_RUN_DIR/$GET_PDB_FILE_SCRIPT ]; then
    echo "$APPTAG ##### ERROR: The get-pdb-file script '$GET_PDB_FILE_SCRIPT' does not exist or is not executable. Check settings. #####"
    echo "$APPTAG ##### ERROR: The get-pdb-file script '$GET_PDB_FILE_SCRIPT' does not exist or is not executable. Check settings. #####" >>$ERROR_LOG
    exit 1
fi

CREATE_DSSP_FILE_SCRIPT="./create_dssp_file.sh"
## check dssp script
if [ ! -x $PLCC_RUN_DIR/$CREATE_DSSP_FILE_SCRIPT ]; then
    echo "$APPTAG ##### ERROR: The create-dssp-file script '$CREATE_DSSP_FILE_SCRIPT' does not exist or is not executable. Check settings. #####"
    echo "$APPTAG ##### ERROR: The create-dssp-file script '$CREATE_DSSP_FILE_SCRIPT' does not exist or is not executable. Check settings. #####" >>$ERROR_LOG
    exit 1
fi


## make sure this was executed from the path it is in (./update_db_from_new_pdb_files.sh instead of something like '/some/path/to/update_db_from_new_pdb_files.sh')
if [ ! -f $UPDATEDIR/$CFG_FILE ]; then
    echo "$APPTAG ##### ERROR: Could not find config file at '$UPDATEDIR/$CFG_FILE'. You have to run this script from the directory it is in. #####"
    echo "$APPTAG ##### ERROR: Could not find config file at '$UPDATEDIR/$CFG_FILE'. You have to run this script from the directory it is in. #####" >>$ERROR_LOG
    exit 1
fi



TIME_START=$(date)

############################################   vamos  ###################################################

if [ -z "$1" ]; then
    echo "$APPTAG ##### ERROR: Usage: $0 <PDB_FILE>"
    echo "$APPTAG ##### ERROR: Usage: $0 <PDB_FILE>" >>$ERROR_LOG
    echo "The PDB file should be the path to the gzipped file that was retrieved via rsync from the PDB server. See config for file extension settings. (This script calls scripts to unzip it and create the DSSP file.)"
    exit 1
fi

FLN="$1"

## extract only the file name from the 'path + filename' string (result looks like this: 'pdb8icd.ent.gz')
FILE=$(basename $FLN)

## now get the PDB ID from the file name (it starts at index 3 and is 4 characters long)
PDBID=${FILE:3:4}
#echo "$APPTAG The PDB ID of the file is '$PDBID'."

if [ ! -r "$FLN" ]; then
    echo "$APPTAG $PDBID ##### ERROR: Could not open PDB file at '$FLN'."
    echo "$APPTAG $PDBID ##### ERROR: Could not open PDB file at '$FLN'." >>$ERROR_LOG    
    exit 1
fi


#echo "$APPTAG $PDBID Handling PDB file '$FLN'. Assuming file extension '$REMOTE_PDB_FILE_EXTENSION'."
if [ "$SILENT" = "NO" ]; then
  echo "$APPTAG $PDBID The log directory is '$LOGDIR'."
fi

## remove old protocol files. this is required because we only append to them later.
DBINSERT_LOG="${LOGDIR}/log_proc1pdb_${PDBID}.log"
ERROR_LOG_PLCC="${LOGDIR}/log_plcc_${PDBID}.log"
ERROR_LOG_GET_PDB_FILE="${LOGDIR}/log_getpdb_${PDBID}.log"
ERROR_LOG_CREATE_DSSP_FILE="${LOGDIR}/log_createdssp_${PDBID}.log"

DBINSERT_LOG="/dev/null"
ERROR_LOG_PLCC="/dev/null"
ERROR_LOG_GET_PDB_FILE="/dev/null"
ERROR_LOG_CREATE_DSSP_FILE="/dev/null"

for L_LOGFILE in $DBINSERT_LOG $ERROR_LOG_PLCC $ERROR_LOG_GET_PDB_FILE $ERROR_LOG_CREATE_DSSP_FILE
do
    if [ -f $L_LOGFILE -a -w $L_LOGFILE ]; then
	delete_file_unless_dev $L_LOGFILE
	if [ $? -ne 0 ]; then
	    echo "$APPTAG $PDBID ##### ERROR: Could not delete old logfile '$L_LOGFILE'. Check permissions."
	    echo "$APPTAG $PDBID ##### ERROR: Could not delete old logfile '$L_LOGFILE'. Check permissions." >>$ERROR_LOG
	    echo "$APPTAG $PDBID ##### ERROR: Could not delete old logfile '$L_LOGFILE'. Check permissions." >>$DBINSERT_LOG
	    exit 1
	fi
    fi
done


#touch $DBINSERT_LOG || exit 1


#echo "$APPTAG $PDBID Logging to '$DBINSERT_LOG' for this PDB file." >>$DBINSERT_LOG
#echo "$APPTAG $PDBID Handling PDB file '$FLN'. Assuming file extension '$REMOTE_PDB_FILE_EXTENSION'." >>$DBINSERT_LOG
#echo "$APPTAG $PDBID The PDB ID of the file is '$PDBID'." >>$DBINSERT_LOG
#echo "$APPTAG $PDBID The starting time is '$TIME_START'." >>$DBINSERT_LOG


NUM_HANDLED=0
NUM_SUCCESS=0
NUM_TOTAL_FAIL=0
NUM_PDB_FAIL=0
NUM_DSSP_FAIL=0
NUM_PLCC_FAIL=0

## Remember that this is a list of file names like this: '/srv/www/PDB/data/structures/divided/pdb/ic/pdb8icd.ent.gz'. So we need to extract the
##  PDB ID from that name, then run the insert scripts for that PDB ID.
NUM_FILES=1

let NUM_HANDLED++

if [ -r $FLN ]; then	


	cd $PLCC_RUN_DIR
	if [ $? -ne 0 ]; then
	    echo "$APPTAG $PDBID ##### ERROR: Cannot cd to plcc_run directory '$PLCC_RUN_DIR'."
	    echo "$APPTAG $PDBID ##### ERROR: Cannot cd to plcc_run directory '$PLCC_RUN_DIR'." >>$ERROR_LOG
            echo "$APPTAG $PDBID ##### ERROR: Cannot cd to plcc_run directory '$PLCC_RUN_DIR'." >>$DBINSERT_LOG
	    del_output $PDBID
	    report_and_exit 1
	fi
	
	if [ "$SILENT" = "NO" ]; then
	  echo "$APPTAG $PDBID Changed directory to '`pwd`'."
	fi
	
	## Get the PDB file
	GET_PDB_FILE_COMMAND="$GET_PDB_FILE_SCRIPT $PDBID"
	if [ "$SILENT" = "NO" ]; then
	    echo "$APPTAG $PDBID The command to get the PDB file is '$GET_PDB_FILE_COMMAND'."
	fi
	$GET_PDB_FILE_COMMAND 1>>$DBINSERT_LOG 2>>$ERROR_LOG_GET_PDB_FILE
	#$GET_PDB_FILE_COMMAND

	if [ $? -ne 0 ]; then
	    echo "$APPTAG $PDBID ##### ERROR: Could not get PDB file for protein '$PDBID', skipping protein '$PDBID'."
	    echo "$APPTAG $PDBID ##### ERROR: Could not get PDB file for protein '$PDBID', skipping protein '$PDBID'." >>$ERROR_LOG
	    echo "$APPTAG $PDBID ##### ERROR: Could not get PDB file for protein '$PDBID', skipping protein '$PDBID'." >>$DBINSERT_LOG
	    let NUM_TOTAL_FAIL++
	    let NUM_PDB_FAIL++
	    del_output $PDBID
	    report_and_exit 1
	else
	    if [ "$SILENT" = "NO" ]; then
	        echo "$APPTAG $PDBID Retrieved the PDB file (unpacked, split and copied), script reported success."
	    fi
	fi

	## double-check it
	PDBFILE="$PDBID.pdb"
	if [ ! -f "$PDBFILE" ]; then
	    echo "$APPTAG $PDBID ##### ERROR: PDB file '$PDBFILE' not found even though creation looked good."
	fi

	## Now create the DSSP file
	CREATE_DSSP_COMMAND="$CREATE_DSSP_FILE_SCRIPT $PDBFILE"
	if [ "$SILENT" = "NO" ]; then
	    echo "$APPTAG $PDBID The command to create the DSSP file is '$CREATE_DSSP_COMMAND'."
	fi
	$CREATE_DSSP_COMMAND 1>>$DBINSERT_LOG 2>>$ERROR_LOG_CREATE_DSSP_FILE
	#$CREATE_DSSP_COMMAND

	if [ $? -ne 0 ]; then
	    echo "$APPTAG $PDBID ##### ERROR: Could not create DSSP file from PDB file '$PDBFILE', skipping protein '$PDBID'."
	    echo "$APPTAG $PDBID ##### ERROR: Could not create DSSP file from PDB file '$PDBFILE', skipping protein '$PDBID'." >>$ERROR_LOG
	    echo "$APPTAG $PDBID ##### ERROR: Could not create DSSP file from PDB file '$PDBFILE', skipping protein '$PDBID'." >>$DBINSERT_LOG
	    let NUM_TOTAL_FAIL++
	    let NUM_DSSP_FAIL++
	    del_output $PDBID
	    report_and_exit 1
	else
	    if [ "$SILENT" = "NO" ]; then
	        echo "$APPTAG $PDBID Created the DSSP file, script reported success."
	    fi
	fi

	## double-check it
	DSSPFILE="$PDBID.dssp"
	if [ ! -f "$DSSPFILE" ]; then
	    echo "$APPTAG $PDBID ##### ERROR: DSSP file '$DSSPFILE' not found even though creation looked good."
	fi	


	## Ok, now call plcc to do the real work.
	PLCC_COMMAND="./plcc $PDBID $PLCC_OPTIONS"
	
	if [ "$SILENT" = "NO" ]; then
	    echo "$APPTAG $PDBID PLCC command is '$PLCC_COMMAND'."
	fi
	#echo "$APPTAG $PDBID PLCC command is '$PLCC_COMMAND'." >>$ERROR_LOG
	echo "$APPTAG $PDBID PLCC command is '$PLCC_COMMAND'." >>$DBINSERT_LOG
	
	$PLCC_COMMAND 1>>$DBINSERT_LOG 2>>$ERROR_LOG_PLCC
	#$PLCC_COMMAND

	if [ $? -ne 0 ]; then
	    echo "$APPTAG $PDBID ##### ERROR: Running plcc failed for PDB ID '$PDBID', skipping protein '$PDBID'."
	    echo "$APPTAG $PDBID ##### ERROR: Running plcc failed for PDB ID '$PDBID', skipping protein '$PDBID'." >>$ERROR_LOG
	    echo "$APPTAG $PDBID ##### ERROR: Running plcc failed for PDB ID '$PDBID', skipping protein '$PDBID'." >>$DBINSERT_LOG
	    let NUM_TOTAL_FAIL++
	    let NUM_PLCC_FAIL++
	    del_output $PDBID
	    report_and_exit 1
	else
	    ## everything worked it seems
	    if [ "$SILENT" = "NO" ]; then
	        echo "$APPTAG $PDBID PLCC run successfully."
	    fi
	    
	    let NUM_SUCCESS++
	    #del_output $PDBID
	    ## we delete the log files if everything went fine
	    for LF in $DBINSERT_LOG $ERROR_LOG_GET_PDB_FILE $ERROR_LOG_CREATE_DSSP_FILE $ERROR_LOG_PLCC
    	    do
	    	if [ -w $LF ]; then
		    delete_file_unless_dev $LF
	    	fi
            done
	    
            if [ "$RUN_GRAPHLETANALYSER" = "YES" ]; then
                if [ "$SILENT" = "NO" ]; then
                    echo "$APPTAG Running Graphletanalyser to compute graphlets."
                fi
		CHAINS_FILE="${PLCC_OUTPUT_DIR}/${PDBID}.chains"
		if [ ! -f "$CHAINS_FILE" ]; then
		       echo "$APPTAG ##### ERROR: No chains file found at '$CHAINS_FILE', is '--cluster' set as PLCC command line option?"
	        else
			for CHAIN in $(cat ${CHAINS_FILE});
			do
			        if [ "$PLCC_RUNS_IN_SUBDIR_TREE_MODE" = "YES" ]; then
			            ## extract the subdir from the pdbid (it is defined by the 2nd and 3rd letter of the id, e.g., for the pdbid '3kmf', it is 'km')
                                    MID2PDBCHARS=${PDBID:1:2}
			            ALBE_GML_GRAPHFILE="${PLCC_OUTPUT_DIR}/${MID2PDBCHARS}/${PDBID}/${CHAIN}/${PDBID}_${CHAIN}_albe_PG.gml"
			        else
			            ALBE_GML_GRAPHFILE="${PLCC_OUTPUT_DIR}/${PDBID}_${CHAIN}_albe_PG.gml"
			        fi

				if [ -f "$ALBE_GML_GRAPHFILE" ]; then
					./graphletanalyser --useDatabase $ALBE_GML_GRAPHFILE
				else
					echo "$APPTAG ##### ERROR:The albe GML graph file was not found at '$ALBE_GML_GRAPHFILE', cannot run graphletanalyser on it."
				fi
			done

			if [ "$DELETE_CLUSTER_CHAINS_FILE" = "YES" ]; then
				rm "$CHAINS_FILE"
			fi
		fi
	    fi

	    report_and_exit_nolog 0
	fi
else
    echo "$APPTAG $PDBID ##### ERROR: Could not read file '$FLN', skipping."
    echo "$APPTAG $PDBID ##### ERROR: Could not read file '$FLN', skipping." >>$ERROR_LOG
    echo "$APPTAG $PDBID ##### ERROR: Could not read file '$FLN', skipping." >>$DBINSERT_LOG
    let NUM_TOTAL_FAIL++
    let NUM_PDB_FAIL++
    del_output $PDBID
    report_and_exit 1
fi	



