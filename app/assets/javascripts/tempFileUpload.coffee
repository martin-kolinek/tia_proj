define(["jquery-1.9.0.min", "tempFileRoutes"], ->
    upload : (fileup, hidden, anchor) ->
        $("document").ready ->
            file = $("#"+fileup)
            file.change ->
                if(file.prop('files').length == 0)
                    return
                toSend = file.prop('files')[0]
                reader = new FileReader()
                reader.onload = (event) ->
                    tempFileRoutes.controllers.TemporaryFileManager.upload().ajax
                        data:event.target.result   
                        contentType:false
                        processData:false
                        cache:false
                        success: (response, status, xhr) ->
                            $("#"+hidden).val(response)
                            $("#"+anchor).prop("href", tempFileRoutes.controllers.TemporaryFileManager.download(response).url)
                        error: (xhr, status, error) -> alert("error: "+error)
                reader.readAsArrayBuffer(toSend)
                
)