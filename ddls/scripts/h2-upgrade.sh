# This is an example. Replace the parameters accordingly

# Back up existing data running on old version of H2
echo SCRIPT COLUMNS NOPASSWORDS TO \'export.sql\' > backup.sql && java -cp h2-1.4.200.jar org.h2.tools.RunScript -url "jdbc:h2:~/old-db" -user sa -script ./backup.sql

# Restore data using new version of H2
java -cp h2-2.0.206.jar org.h2.tools.RunScript -url "jdbc:h2:~/new-db" -user sa -script export.sql

# Run alter script to add new column to database
java -cp h2-2.0.206.jar org.h2.tools.RunScript -url "jdbc:h2:~/new-db" -user sa -script ddls/add-codec/h2-alter.sql
