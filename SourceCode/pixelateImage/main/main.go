package main


import (
	"fmt"
	"image"
	"image/png"
	"os"
	"image/draw"
	"image/color"
	// "image/color"
	// "path/filepath"
)
const sizeOfSquare = 25
//parseImage takes in a path to an image file, and returns a 3d array containing the average RGB value of a given 50
func parseImage(url string) [][][]uint64{
	picture, err := os.Open(url)
	if err != nil {
		fmt.Println("Error processing image at: ", url);
		return make([][][] uint64,0)
	}
	defer picture.Close()

	img, err := png.Decode(picture)
	imgWidth := img.Bounds().Dx()
	imgHeight := img.Bounds().Dy()

	//channel to take packages
	packages := make(chan workerPackage)

	//averageColorArray [x][y][0] gets the average Red value of the block whose top left corner is at x*sizeOfSquare, y*sizeOfSquare
	//averageColorArray [x][y][1] gets the average Green value of the block whose top left corner is at x*sizeOfSquare, y*sizeOfSquare
	//averageColorArray [x][y][2] gets the average Blue value of the block whose top left corner is at x*sizeOfSquare, y*sizeOfSquare
	averageColorArray := make([][][] uint64, imgWidth/sizeOfSquare)
	for i := range averageColorArray {
		averageColorArray[i] = make([][]uint64, imgHeight/sizeOfSquare)
		for j := range averageColorArray[i] {
			averageColorArray[i][j] = make([]uint64, 3)
		}
	}

	for ySlot := 0 ; ySlot < imgHeight/sizeOfSquare; ySlot++{
		for xSlot := 0 ; xSlot < imgWidth/sizeOfSquare; xSlot++{
			//starts up to 40 go routines to process a row of 50x50 pxs of a given image.
			counter := 0
			for counter = 0; (counter < 40 && xSlot < imgWidth/sizeOfSquare); counter++{
				go startWork(img,xSlot,ySlot,packages)
				// fmt.Printf("DEBUG: Processing Block at X:%-5v Y:%-5v\n",xSlot*sizeOfSquare,ySlot*sizeOfSquare)
				if (counter != 39){xSlot++}
				
			}
			for channelCounter := 0; (channelCounter < counter); channelCounter++{
				acceptedPackage := <- packages
				// fmt.Printf("DEBUG: Average Color at Block X: %-5v Y: %-5v = Red: %-3v Blue %-3v Green %-3v \n",acceptedPackage.xSlot*sizeOfSquare,acceptedPackage.ySlot*sizeOfSquare,acceptedPackage.averageColors[0],acceptedPackage.averageColors[1],acceptedPackage.averageColors[2])
				averageColorArray[acceptedPackage.xSlot][acceptedPackage.ySlot][0] = acceptedPackage.averageColors[0];
				averageColorArray[acceptedPackage.xSlot][acceptedPackage.ySlot][1] = acceptedPackage.averageColors[1];
				averageColorArray[acceptedPackage.xSlot][acceptedPackage.ySlot][2] = acceptedPackage.averageColors[2];
			}
			// fmt.Println("DEBUG: Done accepting Packages, processing more blocks");
		}
		// fmt.Println("DEBUG: Done with row Y = ",ySlot);
	}
	// for y := 0 ; y < imgHeight/sizeOfSquare; y++{
	// 	for x := 0 ; x < imgWidth/sizeOfSquare; x++{
	// 		fmt.Printf("DEBUG: Average Color at Block X: %-5v Y: %-5v = Red: %-3v Blue %-3v Green %-3v \n",x*sizeOfSquare,y*sizeOfSquare,averageColorArray[x][y][0],averageColorArray[x][y][1],averageColorArray[x][y][2])
	// 	}
	// } 
	return averageColorArray;
}

type workerPackage struct{
	xSlot int
	ySlot int
	averageColors [] uint64
}
//export startWork
func startWork(image image.Image, xSlot int, ySlot int, packages chan workerPackage){
	averageColors := parseBlock(image, xSlot*sizeOfSquare, ySlot*sizeOfSquare, xSlot*sizeOfSquare + sizeOfSquare, ySlot*sizeOfSquare + sizeOfSquare)
	packages <- workerPackage{xSlot,ySlot,averageColors}
}

//take a block of an image, and determine the average color of all pixels in that block
func parseBlock(image image.Image, minX int,minY int,maxX int,maxY int) []uint64{
	var totalRed uint64
	var totalGreen uint64
	var totalBlue uint64
	totalRed = 0
	totalGreen = 0
	totalBlue = 0

	for y := minY; y < maxY; y++ {
		for x := minX; x < maxX; x++ {
			red , green, blue, _ := image.At(int(x),int(y)).RGBA()
			totalRed += uint64(red);
			totalGreen += uint64(green);
			totalBlue += uint64(blue);
		}
	}
	averageColor := make([]uint64,3)
	numBlocks := uint64((maxY-minY)*(maxX-minX))
	averageColor[0] = (totalRed / numBlocks)/255
	averageColor[1] = (totalGreen / numBlocks)/255
	averageColor[2] = (totalBlue / numBlocks)/255
	//average color can theoretically reach values of 256. Reducing a value of 256 to 255 is necessary to avoid overflow errors.
	if averageColor[0] >= 256 {averageColor[0]=255}
	if averageColor[1] >= 256 {averageColor[1]=255}
	if averageColor[2] >= 256 {averageColor[2]=255}
	return averageColor
}


func main() {
	fmt.Println("Pixelating Image.");
	averageColorArray := parseImage("./public/pictures/resizedImage.png")

	picture, err := os.Open("./public/pictures/resizedImage.png")

	defer picture.Close()
	img, err := png.Decode(picture)
	imgWidth := img.Bounds().Dx()
	imgHeight := img.Bounds().Dy()

	newImage := image.NewRGBA(image.Rect(0, 0, imgWidth, imgHeight))
	for y := 0; y< imgHeight/sizeOfSquare; y++{
		for x := 0; x< imgWidth/sizeOfSquare; x++{
			// fmt.Printf("DEBUG: drawing square at x: %-4v y: %-4v\n",x*sizeOfSquare,y*sizeOfSquare)
			// fmt.Printf("DEBUG Square at x: %-4v y:%-4v has color: Red: %-3v Blue %-3v Green %-3v \n",x*sizeOfSquare,y*sizeOfSquare,uint8(averageColorArray[x][y][0]),uint8(averageColorArray[x][y][1]),uint8(averageColorArray[x][y][2]))
			averageColor := color.RGBA{uint8(averageColorArray[x][y][0]),uint8(averageColorArray[x][y][1]),uint8(averageColorArray[x][y][2]),255}
			corner := image.Point{x*sizeOfSquare,y*sizeOfSquare}

			// fmt.Printf("DEBUG corner.x = %-4v corner.y = %-4v\n",corner.X,corner.Y);
			draw.Draw(newImage, image.Rectangle{image.Point{x*sizeOfSquare,y*sizeOfSquare},image.Point{x*sizeOfSquare+sizeOfSquare,y*sizeOfSquare+sizeOfSquare}}, &image.Uniform{averageColor}, corner, draw.Src)
		}
	}
	
	file, err := os.Create("./public/pictures/pixelatedImage.png")
    if err != nil {
        fmt.Printf("Failed to Create file.")
    }else{
    	err := png.Encode(file, newImage)
    	fmt.Println(err);
    }
    fmt.Println("Pixelated Image.");
}