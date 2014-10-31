$filesWithErrors = @{}
Function FindMatchInLine($file, $line, $attribute)
{
	if($line -match $attribute + '="(.*?)"')
	{
		return $matches[1]
	}
	
	$filesWithErrors[$files.FullName] = $file.FullName
	return ""
}

$files = Get-ChildItem *.cellml -Recurse
$csvRows = @();
foreach ($file in $files)
{
  $content = Get-Content $file;
  
  foreach($line in $content)
  {
	if($line.Contains("<variable"))
	{
		$csvRows += New-Object PsObject -Property @{
			Filename = $file.FullName
			VariableName = FindMatchInLine $file $line "name"
			Units = FindMatchInLine $file $line "units"
		}
	}
  }
}

$csvRows | Export-Csv "CellMLVariableInfo.csv" -NoTypeInformation