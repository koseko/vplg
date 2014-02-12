 <!DOCTYPE html>

<html>
	<head>
		<meta charset="utf-8">
		<meta http-equiv="X-UA-Compatible" content="IE=edge">
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<meta name="description" content="">
		<meta name="author" content="">
		<link rel="shortcut icon" href="../../docs-assets/ico/favicon.png">

		<title>PTGL 2.0</title>


		<!-- Mobile viewport optimized -->
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scale=1.0, user-scalable=no"/>
		

		<!-- Bootstrap CSS -->
		<link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css">
		<link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap-glyphicons.css">


		<!-- Custom CSS -->
		<link rel="stylesheet" type="text/css" href="custom/css/styles.css">

		
		<!-- Include Modernizr in the head, before any other JS -->
		<script src="bootstrap/js/modernizr-2.6.2.min.js"></script>
		
		<!-- Live Search for PDB IDs -->
		<script type="text/javascript">
			$(document).ready(function () {                            
				$("input#searchInput").live("keyup", function(e) {                        
					}
				)};
		</script>/* still needs to be modified */	
	</head>

	
	<body id="customBackground">
		<div class="wrapper">
		<div class="container">
		
		
		<div class="navbar navbar-fixed-top" id="navColor">

				<div class="container">

				<button class="navbar-toggle" data-target=".navbar-responsive-collapse" data-toggle="collapse" type="button">
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
					<span class="icon-bar"></span>
				</button>
				

				<a href="index.html" class="navbar-brand"><img src="ADD_IMAGE_HERE" alt="PTGL Logo"></a>

					<div class="nav-collapse collapse navbar-responsive-collapse">
						<ul class="nav navbar-nav">
							<li  class="navbarFont">
								<a href="index.html">Home</a>
							</li>

							<li class="navbarFont">
								<a href="#About">About</a>
							</li>
						
							<li class="navbarFont">
								<a href="#User Guide">User Guide</a>
							</li>
							
							<li class="navbarFont">
								<a href="#Database Format">Database Format</a>
							</li>		

							<li class="dropdown">
								<!-- <strong>caret</strong> creates the little triangle/arrow -->
								<a href="#"  class="navbarFont dropdown-toggle" data-toggle="dropdown"> Services <strong class="caret"></strong></a>
								
								<ul class="dropdown-menu">
									<li>
										<a href="#">Content</a>
									</li>
									
									<li>
										<a href="#">Publications</a>
									</li>
									
									<li>
										<a href="#">File Formats</a>
									</li>
									
									<!-- divider class makes a horizontal line in the dropdown menu -->
									<li class="divider"></li>
									
									<li class="dropdown-header"></li>
									
									<li>
										<a href="#">Contact Us</a>
									</li>
									
									<li>
										<a href="#">Help</a>
									</li>
								</ul><!-- end dropdown menu -->
							</li><!-- end dropdown -->
						</ul><!-- end nav navbar-nav -->								
					</div><!-- end nav-collapse -->
					<div class="nav-collapse collapse navbar-responsive-collapse">
						<form  class="navbar-form pull-right" action="searchResults.php" method="post">
							<input type="text" class="form-control" id="searchInput" placeholder="Enter PDB ID or keyword...">
							<button type="submit" class="btn btn-default"><span class="glyphicon glyphicon-search"></span></button>
						</form><!-- end navbar-form -->	
					</div>
				</div><!-- end container -->
			</div><!-- end navbar fixed-top -->
		
		<div class="container" id="contactUs">
			<h2> Contact Us </h2>
			<br>
			<h4> List of people associated with PTGL </h4>
			<div class="contact">
				<div id="firstElement">
					<div class="leftColumn" id="member1">
						<strong>Prof. Dr. Ina Koch</strong>
						<br>
						Johann Wolfgang Goethe-University Frankfurt a. Main
						<br>
						Faculty of Computer Science and Mathematics, Dept. 12
						<br>
						Institute for Computer Science
						<br>
						Molecular Bioinformatics (MBI)
						<br>
						Robert-Mayer-Strasse 11-15
						<br>
						60325 Frankfurt a. Main
						<br>
						Germany
					</div><!-- end leftColumn and member1 -->
					
					<div class="rightColumn" id="member1info">
						<br>
						Phone  +49 +69 798-24652
						<br>
						Fax    +49 +69 798-24650
						<br>
						e-mail:  ina.koch (at) bioinformatik.uni-frankfurt.de
						<br>
						http://www.bioinformatik.uni-frankfurt.de/
					</div><!-- end rightColumn and member1info -->
				</div><!-- end firtElement -->
				
				<div id="secondElement">
					<div class="leftColumn" id="member2">
						<strong>Dr. Patrick May</strong>
						<br>
						Max Planck Institute of Molecular Plant Physiology
						<br>
						Am Muehlenberg 1
						<br>
						14476 Potsdam-Golm
						<br>
						Germany
					</div><!-- end leftColumn and member2 -->
					
					<div class="rightColumn" id="member2info">
						<br>
						Phone  +49 331 567-8615
						<br>
						e-mail:  may (at) mpimp-golm.mpg.de
						<br>
						http://bioinformatics.mpimp-golm.mpg.de/group-members/patrick-may
					</div><!-- end rightColumn and member2info -->
				</div><!-- end secondElement -->
				
				
				
				<div id="thirdElement">
					<div class="leftColumn" id="member3">
						<strong>Dr. Thomas Steinke</strong>
						<br>
						Zuse Institute Berlin
						<br>
						Computer Science Research
						<br>
						Takustrasse 7
						<br>
						14195 Berlin
						<br>
						Germany
					</div><!-- end leftColumn and member3 -->
					
					
					<div class="rightColumn" id="member3info">
					<br>
						e-mail:  steinke (at) zib.de
					</div><!-- end rightColumn and member3info -->
				</div><!-- end thirdElement -->
			</div><!-- end contact -->
	
	
	
	
	
	
	
	
	
	
	
	
	
		</div><!-- end container -->
		</div><!-- end container -->
		</div><!-- end wrapper -->
	
	
	<footer id="footer">
		<div class="container">
			<div class="row">
				<div class="col-sm-2">
				</div>
			
				<div class="col-sm-2">
					<a href="#">Impressum</a>
				</div>
				
				<div class="col-sm-2">
					<a href="contact.php">Contact</a>
				</div>
				
				<div class="col-sm-2">
					<a href="http://www.bioinformatik.uni-frankfurt.de" target="_blank">MolBi - Group</a>
				</div>
				
				<div class="col-sm-2">
					<a href="#">Publications</a>
				</div>
				
				<div class="col-sm-2">
				</div>
			
			<div class="row">
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				<div class="col-sm-1">
				</div>
				
				<div class="col-sm-2 flush-right">
					<br>
					<br>
					Copyright © 2013 [name]
				</div>
			</div>
						</div><!-- end container -->
	</footer>
		
		















		<!-- All Javascript at the bottom of the page for faster page loading -->
		<!-- also needed for the dropdown menus etc. ... -->
		
		<!-- First try for the online version of jQuery-->
		<script src="http://code.jquery.com/jquery.js"></script>
		
		<!-- If no online access, fallback to our hardcoded version of jQuery -->
		<script>window.jQuery || document.write('<script src="bootstrap/js/jquery-1.8.2.min.js"><\/script>')</script>
		
		<!-- Bootstrap JS -->
		<script src="bootstrap/js/bootstrap.min.js"></script>
		
		<!-- Custom JS -->
		<script src="bootstrap/js/script.js"></script>





	</body>
</html>