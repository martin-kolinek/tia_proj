require(["jquery.dataTables"], ->
    $(document).ready ->
        $("#main_list").dataTable(
            bFilter:false
            aoColumns: [
                null,
                null,
                {bSortable:false}
            ]
        )
)