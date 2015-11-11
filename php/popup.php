<?
?>

<!DOCTYPE html>

<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>System Launcing</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">
    <link href="css/bootstrap.css" rel="stylesheet">
    <style>
      body {
        padding-top: 60px; /* 60px to make the container go all the way to the bottom of the topbar */
      }
      dt { padding-top:30px; }
      dd { padding-top:30px; }
    </style>
    <link href="css/bootstrap-responsive.css" rel="stylesheet">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="js/html5shiv.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <link rel="apple-touch-icon-precomposed" sizes="144x144" href="ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="ico/apple-touch-icon-57-precomposed.png">
    <link rel="shortcut icon" href="ico/favicon.png">
  </head>
  <body>
    <div class="navbar navbar-inverse navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="brand" href="#">Number Plate</a>
        </div>
      </div>
    </div>

    <div class="container">
	<div class="bs-docs-header" id="content" tabindex="-1">
		<div class="container">
		        <h1>Job is running now</h1>
		        <p>Wait for a moment.</p>
		</div>
	</div>
    </div> <!-- /container -->



    <!-- Le javascript

    ================================================== -->

    <!-- Placed at the end of the document so the pages load faster -->

    <script src="js/jquery.js"></script>

    <script src="js/bootstrap-transition.js"></script>

    <script src="js/bootstrap-alert.js"></script>

    <script src="js/bootstrap-modal.js"></script>

    <script src="js/bootstrap-dropdown.js"></script>

    <script src="js/bootstrap-scrollspy.js"></script>

    <script src="js/bootstrap-tab.js"></script>

    <script src="js/bootstrap-tooltip.js"></script>

    <script src="js/bootstrap-popover.js"></script>

    <script src="js/bootstrap-button.js"></script>

    <script src="js/bootstrap-collapse.js"></script>

    <script src="js/bootstrap-carousel.js"></script>

    <script src="js/bootstrap-typeahead.js"></script>

    <script>
	$(document).ready(function(){
		$.ajax({
			url:'/run_hadoop_application.php',
			type:'get',
			success: function(data){
				alert("Job is successfully finished");
				window.opener.location.reload();
				window.close();
			},
			error: function () {
				alert("System is now unavailable");
			}
		});
		window.opener.location.reload();
	
        });
    </script>
  </body>

</html>
