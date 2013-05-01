require(["common"], (common) ->
    require(["jquery", "dataTables"], ($, dt) ->
        $("#main_list").dataTable(
            bFilter:false
            bLengthChange:false
            aoColumns: [
                null,
                null,
                {bSortable:false}
            ]
        )
    )
)