<?
  $output;
  exec('hadoop dfs -cat project/output/part-r-00000', $output);
  print_r($output);
?>
