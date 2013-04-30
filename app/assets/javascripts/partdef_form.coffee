require(["common"], (common) ->
    require(["temp_files"], (temp) ->
        temp.create("main_file")
    )
)