<?

  $result;
  exec('bash /home/ubuntu/shell/timestamp.sh', $result);
  print_r($result);
  $output;
  exec('bash /home/ubuntu/hipi/run-tmh.sh &', $output);
  print_r($output);
?>
<script>
  location.href="/index.php";
</script>
