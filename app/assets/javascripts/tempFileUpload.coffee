define(["jquery-1.9.0.min", "tempFileRoutes"],  ->
    upload : (fileup) ->
        $("document").ready ->
            $("#"+fileup).change ->
                alert(tempFileRoutes)
)