create keyspace if not exists datastax_unique_versions_demo WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1' };

use datastax_unique_versions_demo;

create table  if not exists sequence(
    id text PRIMARY KEY,
    sequence int
);
