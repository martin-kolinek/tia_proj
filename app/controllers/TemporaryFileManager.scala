package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import scala.collection.mutable.HashMap
import play.api.Play.current
import play.api.libs.Files.TemporaryFile
import resource._
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import play.api.libs.concurrent._
import scala.concurrent.duration._
import play.api.libs.concurrent.Execution.Implicits._

object TemporaryFileStorage {
	private val hm = new HashMap[String, TemporaryFile]
	private var id = 1
	private def nextId = {
		id += 1
		"%010d".format(id)
	} 
	def addFile(file:TemporaryFile) = synchronized {
		val ref = nextId
		hm(ref) = file
		Akka.system.scheduler.scheduleOnce(10.minutes) {
			deleteFile(ref)
		}
		ref
	}
	
	def deleteFile(ref:String) = synchronized {
		hm(ref).clean
		hm.remove(ref)
	}
	
	def hasFile(ref:String) = synchronized {
		hm.isDefinedAt(ref)
	}
	
	def createFile(data:Array[Byte]) = {
		val tf = TemporaryFile("created")
		for(wr <- managed(new FileOutputStream(tf.file))) {
			wr.write(data)
			wr.close()
		}
		synchronized {
			val res = nextId
			hm(res)=tf
			res
		}
	}
  
    def getFile(ref:String) = synchronized {
        for{
			file <- hm.get(ref)
			manag = managed(scala.io.Source.fromFile(file.file))
		} yield manag.acquireAndGet(_.map(_.toByte).toArray)
    }
}

object TemporaryFileManager extends Controller {	
	def upload = Action(parse.temporaryFile){ request =>
		Ok(TemporaryFileStorage.addFile(request.body))
	}
  
    def download(ref:String) = Action{
        TemporaryFileStorage.getFile(ref).map{
    		Ok(_).withHeaders("Content-Disposition" -> "attachment; filename=file.txt")
    	}.getOrElse(NotFound)
    }
	
	val tempFileMapping = nonEmptyText.
			transform(TemporaryFileStorage.getFile, {
				case None => ""
				case Some(data) => TemporaryFileStorage.createFile(data)
			}:Option[Array[Byte]] => String).
			verifying("non existent file reference", _.isDefined).
			transform(_.get, (x:Array[Byte]) => Some(x))
}
