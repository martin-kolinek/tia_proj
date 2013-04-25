define(["tempFileRoutes"], ->
    upload : (fileup, hidden, anchor, change) ->
    	fixDisp = ->
    	    if($("#"+hidden).val())
                $("#"+fileup).hide()
                $("#"+anchor).show()
                $("#"+change).show()
            else       
                $("#"+fileup).show()
                $("#"+anchor).hide()
                $("#"+change).hide()
        $("document").ready ->
            $("#"+fileup).change ->
                file = $(this)
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
                            fixDisp()
                        error: (xhr, status, error) -> 
                            alert("error: "+error)
                            fixDisp()
                reader.readAsArrayBuffer(toSend)
            $("#"+change).click ->
                $("#"+hidden).val("")
                $("#"+fileup).replaceWith($("#"+fileup).clone(true))
                $("#"+fileup).val("")
                fixDisp()
            fixDisp()
                
)