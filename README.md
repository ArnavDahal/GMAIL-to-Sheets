Description:
This program will take UNREAD emails from the Gmail Inbox and create a new spreadsheet with the latest day and time and add them into there.

IN-DEPTH:
The program creates a NEW sheet in the accounts google drive with the Date of the most recent email.
It uses the GMAIL API to parse through the inbox and retrieve unread emails.
It then converts those emails to MimeMessages and retrieves the subject and sent date.
It then calls a method in the Gmail Sheets API which will add the subject and sent date to the created sheets.



TESTS:
I have written a bash script that will email my gmail account from a linux command line.
It uses the sendmail package
To use the bash script, place it into a directory on a linux machine
Then run the following command
./email SUBJECT MESSAGE
Where SUBJECT is the email subeject, and MESSAGE is the email body.
SUBJECT and MESSAGE can also be left blank and it will send a default subject and body.
When I created this I ran it on a personal dedicated server running Ubuntu 14.04.



HOW TO RUN:
Open the .project file in Eclipse Studio
Go to the Gradle configuration and Go to Gradle -> Refresh Gradle
Run Program from the class labeled Main

