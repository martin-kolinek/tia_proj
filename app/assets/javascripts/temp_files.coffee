define(["jquery", "router"], ($, router)->
    create : (handle) ->
    	hidden = handle+"_hf"
    	choose = handle+"_choose"
    	change = handle+"_change"
    	download = handle+"_down"
    	fileup = handle + "_file"
    	fixDisp = ->
    	    if($("."+hidden).val())
                $("."+choose).hide()
                $("."+download).show()
                $("."+change).show()
            else       
                $("."+choose).show()
                $("."+download).hide()
                $("."+change).hide()
        $("document").ready ->
            $("."+fileup).change ->
                file = $(this)
                if(file.prop('files').length == 0)
                    return
                toSend = file.prop('files')[0]
                reader = new FileReader()
                reader.onload = (event) ->
                    router.controllers.TemporaryFileManager.upload().ajax
                        data:event.target.result   
                        contentType:false
                        processData:false
                        cache:false
                        success: (response, status, xhr) ->
                            $("."+hidden).val(response)
                            $("."+download).prop("href", router.controllers.TemporaryFileManager.download(response).url)
                            fixDisp()
                        error: (xhr, status, error) -> 
                            alert("error: "+error)
                            fixDisp()
                reader.readAsArrayBuffer(toSend)
            $("."+change).click ->
                $("."+hidden).val("")
                fixDisp()
                $("."+choose).trigger("click")
            $("."+choose).click ->
            	$("."+fileup).trigger("click")
            fixDisp()
                
)