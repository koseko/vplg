<?php
/** This file stores the configuration and the database connection information
 *  in an array in returns it.
 *   
 * @author Daniel Bruness <dbruness@gmail.com>
 * @author Andreas Scheck <andreas.scheck.home@googlemail.com>
 * @author Tim Schäfer
 */

/**
 * Returns the configuration data in an associative array
 */

//---- Database settings ----//
$DB_HOST		= '127.0.0.1';
$DB_USER		= 'vplg';
$DB_PASSWORD		= 'vplg';
$DB_NAME		= 'vplg';
$DB_PORT		= '5432';
//---- End of database settings ----//

//---- Site settings ----//
$SITE_TITLE = "PTGL";
$TITLE_SPACER = " -- ";
//$SITE_BASE_URL="http://ptgl.uni-frankfurt.de";
$SITE_BASE_URL="http://127.0.0.1/~ts/PTGL3";
//---- End of site settings ----//	

//---- Search Settings ----//
$ENABLE_COMPLEX_GRAPHS	= TRUE;
$ENABLE_MOTIF_SEARCH	= TRUE;
$ENABLE_MOTIF_SEARCH_ALPHABETA = FALSE;
$ENABLE_BLAST_SEARCH	= FALSE;
$ENABLE_GRAPHLETSIMILARITY_SEARCH = FALSE;
$ENABLE_CUSTOMLINNOT_SEARCH = TRUE;
$ENABLE_RANDOM_SEARCH = TRUE;

$USE_LOGIC_OPERATORS	= FALSE;
$USE_PRECOMPUTED_GRAPHLET_SIMILARITY_DATA_FROM_DB = TRUE;
//---- End of search settings ----//

$USE_DENORMALIZED_DB_FIELDS = TRUE;
$CHECK_INSTALL_ON_FRONTPAGE = TRUE;
$CHECK_INSTALL_ON_MAINTENANCEPAGE = TRUE;
$SHOW_MAINTENANCE_MESSAGE_ON_FRONTPAGE = FALSE;
$MAINTENANCE_MESSAGE = "<b>INFO:</b> We are currently doing some maintenance on this server. If you experience problems, please try again when this message has been removed.";

//---- File settings ----//
$BUILD_FILE_PATH	= FALSE;
$IMG_ROOT_PATH		= './data/';
$TMP_DATA_DIR		= './temp_data/';
$TMP_DOWNLOAD_DIR	= './temp_downloads/';
//---- End of file settings ----//

// Maintenance.php users
$MAINTENANCE_USERS = array("ts", "nodi");
$MAINTENANCE_TOKENS_MD5 = array("c8bd0177e53c5d2fec5d7e8cba43c505", "9d07535b8ff70398d39c78c9d19e257e");


//---- Debug settings ----//
$DEBUG_MODE = TRUE;
$DEBUG = $DEBUG_MODE;
//---- End of debug settings ----//

//EOF
?>
