requirejs.config
    paths:
    	"dataTables" : "jquery.dataTables"
    	"router" : "jsRoutes"
    shim:
    	"dataTables" : ["jquery"]
    	"bootstrap" : ["jquery"]
    	"router" : 
    	   deps : ["jquery"]
    	   exports : "jsRouter"
    	   
