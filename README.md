# asset-cli
`asset-cli` is a command-line tool that serializes and manages data tables in protobuf format for sharing static data between layers in the server-client model.

![screenshot of asset-cli](https://user-images.githubusercontent.com/12756996/123517170-77706000-d6da-11eb-9610-3b3daa5a9db4.png)

## Pre requirement
| Dependence                 | Version                    |
| -------------------------- | -------------------------- |
| JDK                        | 16.+                       |
| protoc                     | 3.+                        |

The above dependencies are required to build and run the project.

## Run

`Shell Script` are provided to run on `macOS` and `Linux`.  
In `Windows` environment, it is recommended to run in `Ubuntu WSL` environment using [Windows Terminal](https://docs.microsoft.com/en-us/windows/terminal/get-started)

In the future, if the `native-image` of `GraalVM` supports the runtime external dynamic class loading function, we plan to provide it as a binary.

## Usage examples

```
./asset.sh gen-proto --package=com.sample.vo.asset --scope=CLIENT --schema_dist=schema/ sample/
```

The `.proto` file is created by parsing the `*.xlsx` datasheet in the `sample/` path to the client and share according to the `-scope` option.  
`--package` option is the package name of the proto file to be created

---

```
./asset.sh gen-struct --java_out=struct schema/
```

The `--java_out` option is that of `*_out` arguments in `protoc`.  
Compile the `*.proto` file in the desired language and output it to the `schema/` path.  
In this case, it compiles to the `Java` language.

---

```
./asset.sh serialize --dist=data/ --package=com.sample.vo.asset --scope=CLIENT --struct=struct sample/
```

Serialize the `*.xlsx` datasheets in `sample/` using the structure created by the `gen-struct` command.

---

```
./asset.sh integrity --package=com.sample.vo.asset sample/
```

Check the consistency of the dependency relationship between data tables with the link column value defined in the data sheet.  
For example, if the link value of `FooData.columnB` is `BarData`, check whether the values used in `FooData.columnB` are defined as `BarData.code`.