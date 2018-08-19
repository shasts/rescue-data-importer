#### Insert Ushahidi posts into Elasticsearch index

Ushahidi posts are spread in variuous tables like `post_varchar`, `post_text` etc with mapping to a `form_attribute_id`.
For example `form_attribute_id=110` could mean phone number, `76` could mean Location etc with `post_id` being one of the foreign key.

##### Available tools
There are two tools in this project.
1. Parse a CSV export of the table in the format id, form_attribute_id, value and index in Elasticsearch.
2. Connect to database and poll in batches of 100 `id`s and index them in Elasticsearch.

#### How to use ?
[Install  SBT, the Scala build tool](https://www.scala-sbt.org/download.html).

Create binary package
`sbt clean universal:packageZipTarball` Would package tar ball in `target/universal`

Run:
Extract tar by `tar xf rescue-data-importer-1.0-SNAPSHOT.tgz`

1. Run the tool which connects to database and index in batches
`./rescue-data-importer-1.0-SNAPSHOT/bin/seeder`

2.  Run the tool which reads list of CSV files and index in batches
`./rescue-data-importer-1.0-SNAPSHOT/bin/csv-parser ${list of files, space separated}`
