// implementation adapted from https://stackoverflow.com/a/15773267

const http = require("http");
const path = require("path");
const fs = require("fs");

const express = require("express");

const app = express();
const httpServer = http.createServer(app);

const PORT = process.env.PORT || 8000;

httpServer.listen(PORT, () => {
  console.log(`Server is listening on port ${PORT}`);
});
app.use(express.static(path.join(__dirname, "public")));


const multer = require("multer");

const handleError = (err, res) => {
  res
    .status(500)
    .contentType("text/plain")
    // .log(`Error ${err}`);
    .end(`Oops! Something went wrong! ${err}`);
};

const upload = multer({
  dest: "public/pictures"
});

app.get('/', function (req, res) {
   res.sendFile( __dirname + "/public/templates/" + "index.html" );
})



function resize(){
  console.log("Resizing...");
	var exec = require('child_process').exec, child;
	child = exec("java -jar ./resizePicture.jar",
  	function (error, stdout, stderr){
	    console.log('stdout: ' + stdout);
	    console.log('stderr: ' + stderr);
	    if(error !== null){
	      console.log('exec error: ' + error);
	    }
      pixelate();
	});
}
function pixelate(){
  console.log("Pixelating...");
	var exec = require('child_process').exec, child;
	// exec('cd ./public/pictures');
	child = exec("pixelateImage.exe",
  	function (error, stdout, stderr){
	    console.log('stdout: ' + stdout);
	    console.log('stderr: ' + stderr);
	    if(error !== null){
	      console.log('exec error: ' + error);
	    }
	});
	// exec('cd ../..');
}

function reConstitute(){
  console.log("Reconstituting...");
	var exec = require('child_process').exec, child;
	// exec('cd ./public/pictures');
	child = exec("java -jar ./reDrawImage.jar",
  	function (error, stdout, stderr){
	    console.log('stdout: ' + stdout);
	    console.log('stderr: ' + stderr);
	    if(error !== null){
	      console.log('exec error: ' + error);
	    }
	});
	// exec('cd ../..');
}

app.get("/upload",function(req,res){

	res.sendFile( __dirname + "/public/templates/" + "upload.html" );
}
)

app.get("/results",function(req,res){

	res.sendFile( __dirname + "/public/templates/" + "results.html" );
}
)


app.post("/upload", upload.single("file"), (req, res) => {
    const tempPath = req.file.path;
    // const targetPath = path.join(__dirname, "./public/pictures/image.png");

    if (path.extname(req.file.originalname).toLowerCase() === ".png") {
      fs.rename(tempPath, "public/pictures/image.png", err => {
        if (err) return handleError(err, res);
          resize();
          
          res.status(200)
          res.contentType("text/html")
          res.sendFile( __dirname + "/public/templates/" + "upload.html" );
          setTimeout(reConstitute, 2000);
      });
    } else {
      fs.unlink(tempPath, err => {
        if (err) return handleError(err, res);
          res.status(403)
          res.contentType("text/plain")
          res.end("Only .png files are allowed!");
      });
    }
  }
);