require(["common"], (common) ->
    require(["temp_files", "bootstrap"], (temp, bs) ->
        temp.create("main_file")
    )
)