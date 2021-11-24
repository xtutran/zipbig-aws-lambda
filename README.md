# aws-lambda-poc
Zip S3 files up to 10G by using AWS Lambda

## 1. Setup environment
```shell script
brew install direnv
```

- Add your own `.envrc`
```
export AWS_SHARED_CREDENTIALS_FILE=YOUR_AWS_CRED
export PACKAGE_NAME=YOUR_OWN_PACKAGE_NAME
export S3_BUCKET=YOUR_OWN_S3_BUCKET
export IAM_ROLE=YOUR_IAM_ROLE_FOR_YOUR_LAMBDA
```

```shell script
direnv allow
```

## 2. Build `jar` package
```shell script
bash make.sh --build
```

## 3. Deploy `AWS lambda` function
```shell script
bash make.sh --publish
```

## 4. Update `AWS lambda` function
```shell script
bash make.sh --update
```


