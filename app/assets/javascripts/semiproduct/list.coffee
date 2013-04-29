require(["jquery.dataTables"], ->
    $(document).ready ->
        $("#main_list").dataTable(
            bFilter:false
            bAutoWidth:false
            aoColumns: [
                null,
                null,
                null,
                null,
                {bSortable:false, sWidth:"9em"}
            ]
        )
            
)