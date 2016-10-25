{"version":"NotebookV1","origId":503877321547950,"name":"Lab","language":"scala","commands":[{"version":"CommandV1","origId":503877321547952,"guid":"98a3ca52-876d-4a1a-95a2-c29989d7a6d3","subtype":"command","commandType":"auto","position":1.0,"command":"%md \n# Spark Streaming Lab (Scala)","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"80b342bc-9663-442f-ba47-254b6e78f83e"},{"version":"CommandV1","origId":503877321547953,"guid":"412913ab-79a1-4e3b-af7f-6c8c9797e78c","subtype":"command","commandType":"auto","position":2.0,"command":"%md\n## Setup\n\nThis first section is largely just setup. If you want to play around with the configuration constants, feel free to do so. But leave the imports and the `require` alone.","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"9877a3f3-d6d0-45ef-81b8-ba281366559b"},{"version":"CommandV1","origId":503877321547954,"guid":"4b0da7e8-7c0f-4842-9f21-39d6b70adf32","subtype":"command","commandType":"auto","position":3.0,"command":"// Necessary imports and other constants. DON'T change these.\n\nimport com.databricks.training.streaming.RandomWordSource\nimport org.apache.spark._\nimport org.apache.spark.storage._\nimport org.apache.spark.streaming._\nimport org.apache.spark.sql._\n\nval WordsFile = \"dbfs:/mnt/training/word-game-dict.txt\"\n\nval rng = new java.security.SecureRandom\nval randomID = rng.nextInt(1000)\n\n// NOTE: This name has to be unique per-user, hence the random suffix.\nval OutputDir   = \"dbfs:/tmp/words\"\nval ParquetFile = s\"$OutputDir$randomID\"\n\n// Temporary table name. Shouldn't clash with anyone else, unless multiple\n// people are using the same cluster. Just in case, we'll use the random ID.\nval TableName = s\"words_$randomID\"\n\n// Ensure that we're running against Spark 1.4.0 or better.\nrequire(sc.version.replace(\".\", \"\").toInt >= 140, \"Spark 1.4.0 or greater is required to run this notebook. Please attach notebook to a 1.4.x cluster.\")\n\ndbutils.fs.mkdirs(OutputDir)","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"505f6d8e-9371-4b03-8af3-2ea378d66fe2"},{"version":"CommandV1","origId":503877321547955,"guid":"f86c7f07-a504-4caf-9905-8feea74cccd6","subtype":"command","commandType":"auto","position":4.0,"command":"// --------------------------------------------------------------------\n// CONFIGURATION\n// --------------------------------------------------------------------\n\nval WordsPerCycle   = 10000  // A \"cycle\" is just an internal loop within the StreamingWordSource. It's not \n                             // Spark-related at all.\nval InterCycleSleep = 500    // number of milliseconds to wait between each burst of words\n\nval BatchIntervalSeconds = 1\n\nval CheckpointDirectory = Some(s\"$OutputDir/checkpoint/$randomID\")\n\n// RememberSeconds defines the number of seconds that each DStream in the context\n// should remember RDDs it generated in the last given duration. A DStream remembers\n// an RDD only for a limited duration of time, after which it releases the RDD for\n// garbage collection. You can change the duration (which is useful, if you wish to\n// query old data outside the DStream computation).\nval RememberSeconds = Some(60)\n\n// END CONFIGURATION","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"63dbc50d-87ec-4ddf-9072-23d213a12a98"},{"version":"CommandV1","origId":503877321547956,"guid":"9e82efcf-00d3-4aa3-a909-6b631611e6c1","subtype":"command","commandType":"auto","position":5.0,"command":"%md\n## A helper function to create our streaming source of words","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"d574ed14-f1b4-4ca9-a8bd-c40beee79c99"},{"version":"CommandV1","origId":503877321547957,"guid":"1bac172b-cc86-41ba-85e3-abdd4df7ce00","subtype":"command","commandType":"auto","position":6.0,"command":"// Load the dictionary.\nval rdd = sc.textFile(WordsFile)\nval words = rdd.collect()\n\n/** Create and return a Streaming source we can use.\n  */\ndef createRandomWordSource() = {\n  // Create a stream from our RandomWordSource\n  new RandomWordSource(dictionary      = words,\n                       wordsPerCycle   = WordsPerCycle,\n                       interCycleSleep = InterCycleSleep)\n}","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"06d1a2c9-a0f1-42ec-b33f-aeb4e2b81fdd"},{"version":"CommandV1","origId":503877321547958,"guid":"0612fa46-5e40-45db-86cc-f4cfbd07ddfa","subtype":"command","commandType":"auto","position":7.0,"command":"rdd.count","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"2aca6d90-83f2-4088-9f13-554c217aa484"},{"version":"CommandV1","origId":503877321547959,"guid":"693f1ac6-61a1-44f3-b24b-49fa178fb7df","subtype":"command","commandType":"auto","position":8.0,"command":"%md\n## Create our Streaming Process\n\nThe code in the next cell is the meat of our Spark Streaming application. It:\n\n* creates a Streaming source that randomly produces words from the dictionary\n* creates a stream to read from the source\n* configures the stream so that each RDD the stream produces is:\n    * mapped to another RDD that counts the words, which is then\n    * persisted to a Parquet file.\n    \n**NOTE** In Databricks (and in a Scala REPL), it helps to wrap your streaming solution in an object, to prevent scope confusion when Spark attempts to gather up and serialize the variables and functions for distribution across the cluster.","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"9d22fdf5-27a5-4f55-9770-d2cf974b7078"},{"version":"CommandV1","origId":503877321547960,"guid":"dea11522-b6b1-4035-9aeb-f8d85cc21209","subtype":"command","commandType":"auto","position":9.0,"command":"object Runner extends Serializable {\n  \n  case class WordCount(word: String, count: Int)\n\n  def stop(): Unit = {\n    StreamingContext.getActive.map { ssc =>\n      ssc.stop(stopSparkContext = false)\n      println(\"Stopped Streaming Context.\")\n    }\n  }\n  \n  def start(): Unit = {\n    // Create the streaming context.\n    val ssc = CheckpointDirectory.map { checkpointDir =>\n      StreamingContext.getActiveOrCreate(checkpointDir, createStreamingContext _)\n    }.\n    getOrElse {\n      StreamingContext.getActiveOrCreate(createStreamingContext _)\n    }\n\n    // Start our streaming context.\n    ssc.start()\n\n    println(\"Started/rejoined streaming context\")\n    \n    // Wait for it to terminate (which it won't, because the source never stops).\n    //ssc.awaitTerminationOrTimeout(BatchIntervalSeconds * 5 * 1000)\n  }\n\n  def restart(): Unit = {\n    stop()\n    start()\n  }\n  \n  private def createStreamingContext(): StreamingContext = {\n  \n    // Create a StreamingContext\n    val ssc = new StreamingContext(sc, Seconds(BatchIntervalSeconds))\n  \n    // To make sure data is not deleted by the time we query it interactively\n    RememberSeconds.foreach(secs => ssc.remember(Seconds(secs)))\n  \n    // For saving checkpoint info so that it can recover from failed clusters\n    CheckpointDirectory.foreach(dir => ssc.checkpoint(dir))\n  \n    val stream = ssc.receiverStream(createRandomWordSource())\n    \n    // What we're getting is a stream of words. As each RDD is created by\n    // Spark Streaming, have the stream create a new RDD that counts the\n    // words.\n    val wordCountStream = stream.map { word => (word, 1) }.\n                                 reduceByKey(_ + _).\n                                 map { case (word, count) => WordCount(word, count) }\n\n    // Save each RDD in our Parquet table. We'll likely end up with multiple rows for each word, but\n    // we can sum those up easily enough.\n\n    wordCountStream.foreachRDD { rdd => \n      val sqlContext = SQLContext.getOrCreate(SparkContext.getOrCreate())\n      val df = sqlContext.createDataFrame(rdd)\n      df.write.mode(SaveMode.Append).parquet(ParquetFile)\n    }\n    \n    ssc\n  }    \n}","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"5806da7e-f7d5-4f14-a49d-f663d2928861"},{"version":"CommandV1","origId":503877321547961,"guid":"afa2a7cf-af8a-449c-b61d-0a89fc741d8d","subtype":"command","commandType":"auto","position":10.0,"command":"%md Verify that our runner can be serialized.","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"a01e0508-957e-41e9-b59c-808000267f61"},{"version":"CommandV1","origId":503877321547962,"guid":"2c716ec3-c697-4a9f-9700-7f110edd7209","subtype":"command","commandType":"auto","position":11.0,"command":"val out = new java.io.ObjectOutputStream(new java.io.ByteArrayOutputStream)\nout.writeObject(Runner)","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"396870e5-6ab9-4f66-9a7c-f62e458c0398"},{"version":"CommandV1","origId":503877321547963,"guid":"2c9e7bbf-99c9-4be4-a985-f6771beca94d","subtype":"command","commandType":"auto","position":12.0,"command":"%md \n## Start it all up\n\nHere, we start the stream and wait until everything finishes. In this case, it won't finish on its own; it'll continue until we shut it down.","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"3efb04d7-fe18-42df-b2c2-68283c2247e6"},{"version":"CommandV1","origId":503877321547964,"guid":"1450b9bb-2580-41b5-8749-f8b218c82613","subtype":"command","commandType":"auto","position":13.0,"command":"Runner.restart()","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"790dd0af-f940-49ec-8a61-a6d6b7ee62a6"},{"version":"CommandV1","origId":503877321547965,"guid":"a2729f52-b738-4163-b876-29cde6538f60","subtype":"command","commandType":"auto","position":14.0,"command":"%md\n## Query our Parquet file\n\nThe cell, below, loads and queries the Parquet table that's being built by the stream. Run that cell repeatedly, and watch how the data changes. You should see multiple rows for the same word, after awhile.\n\n**Question**: Why is that happening?\n\n**NOTE**: Because of timing issues, the query might occasionally fail with an obscure-looking error. Just reissue it if that happens.","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"c6f35cb0-f632-42e6-a61c-c4352c2534af"},{"version":"CommandV1","origId":503877321547966,"guid":"cf443952-7379-4cf9-98e9-2d3e66a22722","subtype":"command","commandType":"auto","position":15.0,"command":"val df = sqlContext.read.parquet(ParquetFile)\ndisplay(df.orderBy($\"count\".desc))","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"284fe9aa-a9f4-4da2-bfca-5392041356a4"},{"version":"CommandV1","origId":503877321547967,"guid":"775e0121-d868-4006-862a-39142ac74bb4","subtype":"command","commandType":"auto","position":16.0,"command":"%md\n## Shut it down\n\nWait about five minutes, then stop the stream.","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"cf7d5df4-ccaf-4aed-81db-7c9478327a3f"},{"version":"CommandV1","origId":503877321547968,"guid":"c665aa4d-4b05-4661-999b-e16babc3bb45","subtype":"command","commandType":"auto","position":17.0,"command":"Runner.stop()","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"97c6fedd-8cc8-4ce3-941d-753388b7d718"},{"version":"CommandV1","origId":503877321547969,"guid":"3fcaa79a-547d-4ccf-9d1d-36b570ee8e95","subtype":"command","commandType":"auto","position":18.0,"command":"%md\n## Aggregating the counts\n\nLet's aggregate the counts. SQL is as handy a way as any.","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"9b11c0fd-fcee-4f67-8051-46f2565daf13"},{"version":"CommandV1","origId":503877321547970,"guid":"26821453-b4d3-45d0-8c48-09824e5b30ca","subtype":"command","commandType":"auto","position":19.0,"command":"df.registerTempTable(TableName)\ndisplay(sqlContext.sql(s\"SELECT word, sum(count) AS total FROM $TableName GROUP BY word ORDER BY total DESC, word LIMIT 50\"))","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"f9ece965-2df0-4331-b9f0-20d8de56be0f"},{"version":"CommandV1","origId":503877321547971,"guid":"46b23839-eabd-4011-947a-c6b2e9e88632","subtype":"command","commandType":"auto","position":20.0,"command":"%md\n## Assignment\n\nModify the above `Runner` to filter the stream so that it:\n\n* discards words more than 7 characters long,\n* discards words that have a Scrabble base score less than 15,\n* saves the remaining words and point values in a new Parquet table\n* queries that table for the 10 top-scoring words in the stream\n\nA base score is the score of a word in Scrabble _before_ accounting for any multipliers related to board placement (e.g., before applying triple-word scores, double-letter scores, and the like).\n\nThis assignment is a variation of the example above. You can use DataFrame operations or SQL to accomplish the work. You can also re-use `wordsSource`, from above; there's no need to recreate it.\n\n(**NOTE**: This is an imperfect problem statement, because it doesn't take letter frequencies into account. There's only one \"Z\" in a set of Scrabble tiles, but a solution that conforms to the above description will assign 10 points to _every_ \"Z\" in a word that has multiple \"Z\" letters—even though that's impossible in Scrabble. For our purposes, that's fine.)\n\n**The following cell defines the outline of the solution. Your solution code goes at the bottom of the `run()` method.**","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"53770d85-3ab6-4233-ac91-23730e74c1ad"},{"version":"CommandV1","origId":503877321547972,"guid":"6c0485a1-870e-460f-8f7a-6b965de592f8","subtype":"command","commandType":"auto","position":21.0,"command":"val SolutionParquet = s\"$OutputDir/scrabble_$randomID\"\nval SolutionTable   = s\"scrabble_$randomID\"\n\nobject Solution extends Serializable {\n \n  case class WordScore(word: String, score: Int)\n  \n  def stop(): Unit = {\n    StreamingContext.getActive.map { ssc =>\n      ssc.stop(stopSparkContext = false)\n      println(\"Stopped Streaming Context.\")\n    }\n  }\n  \n  def start(): Unit = {\n    // Create the streaming context.\n    val ssc = CheckpointDirectory.map { checkpointDir =>\n      StreamingContext.getActiveOrCreate(checkpointDir, createStreamingContext _)\n    }.\n    getOrElse {\n      StreamingContext.getActiveOrCreate(createStreamingContext _)\n    }\n\n    // Start our streaming context.\n    ssc.start()\n\n    println(\"Started/rejoined streaming context\")\n    \n    // Wait for it to terminate (which it won't, because the source never stops).\n    //ssc.awaitTerminationOrTimeout(BatchIntervalSeconds * 5 * 1000)\n  }\n\n  def restart(): Unit = {\n    stop()\n    start()\n  }\n  \n  private def createStreamingContext(): StreamingContext = {\n    // Use this map to calculate the point value of each word, letter by letter.\n    val LetterPoints = Map('a' ->  1, 'b' ->  3, 'c' ->  3, 'd' ->  2, 'e' ->  1,\n                           'f' ->  4, 'g' ->  2, 'h' ->  4, 'i' ->  1, 'j' ->  8,\n                           'k' ->  5, 'l' ->  1, 'm' ->  3, 'n' ->  1, 'o' ->  1,\n                           'p' ->  3, 'q' -> 10, 'r' ->  1, 's' ->  1, 't' ->  1,\n                           'u' ->  2, 'v' ->  4, 'w' ->  4, 'x' ->  8, 'y' ->  4,\n                           'z' -> 10)\n\n    // Create the streaming context.\n    val ssc = new StreamingContext(sc, Seconds(BatchIntervalSeconds))\n    \n    // ****** PUT YOUR SOLUTION HERE\n\n    // ******\n    \n    ssc\n  }\n}","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"c0bdfa45-3077-4edb-9d67-cdd3a45207f6"},{"version":"CommandV1","origId":503877321547973,"guid":"bdde50e8-48f5-4f48-afe5-f9f96a47d8a2","subtype":"command","commandType":"auto","position":22.0,"command":"Solution.restart()","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"1bb0381f-1d96-4363-8b9b-9555dd666370"},{"version":"CommandV1","origId":503877321547974,"guid":"3d2ee48f-e3a7-4067-bfd0-2ca89f10fe10","subtype":"command","commandType":"auto","position":23.0,"command":"val df = sqlContext.read.parquet(SolutionParquet)\ndf.registerTempTable(SolutionTable)\ndisplay(sqlContext.sql(s\"SELECT word, score, count(score) AS total FROM $SolutionTable GROUP BY word, score ORDER BY score DESC, total DESC, word LIMIT 50\"))","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"f8caa410-4721-4c1a-9a24-cbbb2c956bea"},{"version":"CommandV1","origId":503877321547975,"guid":"671afc78-ace2-4e41-9570-1dcaff962828","subtype":"command","commandType":"auto","position":24.0,"command":"Solution.stop()","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"9abc9f4b-7ada-4ac0-8c3a-a065dc1c4997"},{"version":"CommandV1","origId":503877321547976,"guid":"188eadbb-13d8-49f6-b3e1-d54d4184e011","subtype":"command","commandType":"auto","position":25.0,"command":"","commandVersion":0,"state":"finished","results":null,"errorSummary":null,"error":null,"workflows":[],"startTime":0.0,"submitTime":0.0,"finishTime":0.0,"collapsed":false,"bindings":{},"inputWidgets":{},"displayType":"table","width":"auto","height":"auto","xColumns":null,"yColumns":null,"pivotColumns":null,"pivotAggregation":null,"customPlotOptions":{},"commentThread":[],"commentsVisible":false,"parentHierarchy":[],"diffInserts":[],"diffDeletes":[],"globalVars":{},"latestUser":"","commandTitle":"","showCommandTitle":false,"hideCommandCode":false,"hideCommandResult":false,"iPythonMetadata":null,"streamStates":{},"nuid":"b3a480b0-5aa8-4957-a618-1b51c6cace12"}],"dashboards":[],"guid":"1262bfd7-5426-471f-b16a-41e9607df901","globalVars":{},"iPythonMetadata":null,"inputWidgets":{}}