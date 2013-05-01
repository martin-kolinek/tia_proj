require(["common"], (common) ->
	require(["jquery", "dataTables", "list_util"], ($, dt, lutil)->
        $("#main_list").dataTable(lutil.listDt(5))
    )
)