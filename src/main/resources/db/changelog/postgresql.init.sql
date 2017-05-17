create user rmvote
alter role rmvote with password 'rmvote'
grant all on alltables in schema public to rmvote;