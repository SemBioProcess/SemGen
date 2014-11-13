# Files to parse
$files = Get-ChildItem *.cellml -Recurse

# Jobs that are running in parallel to process each file
$jobs = @();

# Measures the amount of time it takes to process each file
$elapsed = [System.Diagnostics.Stopwatch]::StartNew()

# Loop over each file and parse it
foreach ($file in $files)
{
	# Runs for each file
	# 1) Parse out the variables in the file
	# 2) Return an array of rows containing information about each variable
	$jobScript = { param($fileName)
		
		# Finds an attribute in the given line and returns
		# the attributes value
		Function FindMatchInLine($line, $attribute)
		{
			if($line -match $attribute + '="(.*?)"')
			{
				return $matches[1]
			}
			
			return ""
		}
	
		# Keeps track of the variables we've seen
		# Used to make sure we don't have duplicate entries
		$hasSeenVar = @{}
		
		# All rows containing information about variables in file
		$dataRows = @();
		
		# Get the file's contents
		$file = Get-Item $fileName;
		$content = Get-Content $file;

		# Loop over each file and extract info about each variable
		foreach($line in $content)
		{
			# Does this line have info about a variable?
			if($line.Contains("<variable"))
			{
				# Get the variable name
				$varName = FindMatchInLine $line "name"
			
				# If we've already seen the var for this file don't add another row for it
				if($hasSeenVar.ContainsKey($varName))
				{
					continue;
				}
			
				# Save info about the variable to the collection
				$dataRows += @{
					VariableName = $varName
					Units = FindMatchInLine $line "units"
					ModelName = $file.Name
					ModelCategory = Split-Path (Split-Path -Path $fileName -Parent ) -Leaf
				}
				
				# Remember that we've seen this var
				$hasSeenVar[$varName] = $true;
			}
		}
		
		return $dataRows;
	}

	# If we're running more than 8 jobs wait on one to finish
	# This allows us to run jobs in parallel, while not hogging
	# all the machine resources
	#
	# Note: This could be done better, but this took the least amount of time :)
	$running;
	do
	{
		$running = @(Get-Job | Where-Object { $_.State -eq 'Running' })
		Write-Host "Num jobs running: "$running.Count
		Sleep -s 0.5
	}
	while($running.Count -ge 8)
	
	# Kick off a job to parse the file
	Write-Host "Creating job to read file: " $file.FullName
	$jobs += Start-Job $jobScript -Arg $file.FullName
}

# If there weren't any files return
if($jobs.Count -le 0)
{
	Write-Host "There weren't any files to read"
	return;
}

# Wait for all the jobs we kicked off to finish
Write-Host "Waiting on jobs to finish..."
Wait-Job $jobs

Write-Host "Time to read files: " $elapsed.Elapsed.ToString()

# Create a CSV file from the results of each job
Write-Host "Creating csv"
Receive-Job $jobs | Foreach{ New-Object PsObject -Property @{
			VariableName = $_["VariableName"]
			Units = $_["Units"]
			ModelName = $_["ModelName"]
			ModelCategory = $_["ModelCategory"] } } | Export-Csv "CellMLVariableInfo.csv" -NoTypeInformation
			
Write-Host "Total time: " $elapsed.Elapsed.ToString()