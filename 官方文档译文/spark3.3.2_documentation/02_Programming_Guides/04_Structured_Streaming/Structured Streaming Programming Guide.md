# Structured Streaming Programming Guide

[TOC]

## Overview
## Quick Example
## Programming Model
### Basic Concepts
### Handling Event-time and Late Data
### Fault Tolerance Semantics
## API using Datasets and DataFrames
### Creating streaming DataFrames and streaming Datasets
#### Input Sources
#### Schema inference and partition of streaming DataFrames/Datasets
### Operations on streaming DataFrames/Datasets
#### Basic Operations - Selection, Projection, Aggregation
#### Window Operations on Event Time
##### Handling Late Data and Watermarking
##### Types of time windows
## Join Operations
#### Stream-static Joins
#### Stream-stream Joins
##### Inner Joins with optional Watermarking
##### Outer Joins with Watermarking
##### Semi Joins with Watermarking
## Support matrix for joins in streaming queries
## Streaming Deduplication
## Policy for handling multiple watermarks
## Arbitrary Stateful Operations
## Unsupported Operations
## Limitation of global watermark
## State Store
### HDFS state store provider
### RocksDB state store implementation
#### Performance-aspect considerations
### State Store and task locality
### Starting Streaming Queries
## Output Modes
## Output Sinks
### Using Foreach and ForeachBatch
#### ForeachBatch
#### Foreach
## Streaming Table APIs
## Triggers
### Managing Streaming Queries
### Monitoring Streaming Queries
#### Reading Metrics Interactively
#### Reporting Metrics programmatically using Asynchronous APIs
#### Reporting Metrics using Dropwizard
### Recovering from Failures with Checkpointing
### Recovery Semantics after Changes in a Streaming Query
## Continuous Processing
## Additional Information
## Migration Guide