      var myLoad = function () {
        var images = document.getElementsByTagName("img");
        for (var i = 0; i < images.length; i++) {
          var image = images [i];
          if (image.className == "removed" || image.className == "added") {
            var filter = document.createElement("div");
            filter.className= image.className;
            filter.style.width = image.offsetWidth;
            filter.style.height = image.offsetHeight;
            filter.style.top = image.offsetTop;
            filter.style.left = image.offsetLeft;
            image.parentNode.insertBefore(filter, image);
          }
        }
      }
