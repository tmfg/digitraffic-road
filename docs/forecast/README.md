# Downloading forecast files from S3 locally

Road forecasts (V2) are read from an S3 bucket under the `forecast/` prefix.
File name (see `ForecastSectionS3ClientV2`):

```
forecast/<data set prefix><forecast_start_time>_<created_time>.json
```

Times are UTC in the format `yyyyMMdd'T'HHmmss'Z'`. Example:
`forecast/roadweather_forecast_public_full_20261014T080000Z_20261014T064827Z.json`

| Data set | File name prefix                      |
| -------- | ------------------------------------- |
| DETAILED | `roadweather_forecast_public_full_`   |
| SIMPLE   | `roadweather_forecast_public_simple_` |

## Settings

Do not store the keys in files or in git.

```bash
export AWS_DEFAULT_REGION="eu-west-1"
export AWS_ACCESS_KEY_ID="<AccessKeyId>"
export AWS_SECRET_ACCESS_KEY="<SecretAccessKey>"
export AWS_S3_BUCKET="<BucketName>"
```

## Download

All public files (both `full` and `simple`). `--exclude "*"` must come first:

```bash
aws s3 sync "s3://${AWS_S3_BUCKET}/forecast/" "./${AWS_S3_BUCKET}" \
  --exclude "*" --include "roadweather_forecast_public_*"
```

Only public full:

```bash
aws s3 sync "s3://${AWS_S3_BUCKET}/forecast/" "./${AWS_S3_BUCKET}" \
  --exclude "*" --include "roadweather_forecast_public_full_*"
```

Only public simple:

```bash
aws s3 sync "s3://${AWS_S3_BUCKET}/forecast/" "./${AWS_S3_BUCKET}" \
  --exclude "*" --include "roadweather_forecast_public_simple_*"
```

A single day (forecast start time, UTC), e.g. public full on 2026-11-18:

```bash
aws s3 sync "s3://${AWS_S3_BUCKET}/forecast/" "./${AWS_S3_BUCKET}" \
  --exclude "*" --include "roadweather_forecast_public_full_20261118T*"
```

Several days:

```bash
aws s3 sync "s3://${AWS_S3_BUCKET}/forecast/" "./${AWS_S3_BUCKET}" \
  --exclude "*" \
  --include "roadweather_forecast_public_*_20261117T*" \
  --include "roadweather_forecast_public_*_20261118T*"
```

From a given date onwards. `sync` cannot do this, so list the keys with `--start-after` and copy them one by one.
This example downloads public full files from 2026-11-18 00:00 UTC onwards (change the prefix for `simple`):

```bash
aws s3api list-objects-v2 --bucket "${AWS_S3_BUCKET}" \
  --prefix "forecast/roadweather_forecast_public_full_" \
  --start-after "forecast/roadweather_forecast_public_full_20261118T000000Z" \
  --query "Contents[].Key" --output text | tr '\t' '\n' | while read -r key; do
    aws s3 cp "s3://${AWS_S3_BUCKET}/${key}" "./${AWS_S3_BUCKET}/"
  done
```

List without downloading:

```bash
aws s3 ls "s3://${AWS_S3_BUCKET}/forecast/roadweather_forecast_public_full_20261118"
```

Add `--dryrun` to see what would be downloaded.

Note: the date in the filter is the forecast start time (the first timestamp in the file name). The creation time (the second timestamp) cannot be filtered reliably with `--include`.
