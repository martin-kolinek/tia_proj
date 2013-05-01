require(["common"], (common) ->
    require(["jquery", "dataTables", "list_util"], ($, dt, lutil) ->
        $(document).ready ->
            $("#main_list").dataTable(lutil.listDt(3))
    )
)