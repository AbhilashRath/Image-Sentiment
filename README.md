# Image-Sentiment
An Android Application which helps user to recognise text from an Image and learn about its sentiments in the form of audio. This was our first attempt in developing an Android App where the backend logic was developed in python and the motivation behind this project was to build an accessible sentiment analyser and the urge to learn building machine learning apps.
Firebase ML-Kit was used to host our custom ML model of sentiment analysis and pre-built API of text recognition by Firebase was used to recognise texts from images.

## Sentiments Analysis
We used supervised machine learning classification algorithm to make the sentiment analysis model in python. 
The data was downloaded from here: https://archive.ics.uci.edu/ml/datasets/Sentiment+Labelled+Sentences. 

### Data Preprocessing
We did data preprocessing using NLTK(natural langauge toolkit library) in python. The steps are as follows:
1. Make all alphabets into lowercase.
2. Word tokenizing.
3. Stemming
4. Removed the stopwords like is,this,am.

### Training machine learning model
We used 1000 most frequent words that were in dataset and used them to make the machine learning model.

The machine learning model was made using tensorflow and we made three neural network layers with 128,64,1 nodes respectively, then used sigmoid function on third node to classify the sentiment into 1(positive class) and 0(negative class).

Accuracy on training data was 95 percent.
Accuracy on test data was 85 percent.

### Saving the model
Now, we saved the trained model in .tflite format to use it in the app.

### Integrating with our frontend code in Java
We saved the model sentiment.tflite in the assets folder of our project along with the set of thousand words with their tokens in json format.
Required input format for the tensorflow model: Datatype-Float32, Size-{1,1000}
Output Format of the tensorflow model: Datatype-Float32, Size-{1,1}
First an array of all zeroes of size {1,1000} is declared and for each word in tokenizer.json we check if it is in input text and input 1 or 0 at the word index accordingly in the zeroes array.
We add this input array to the FirebaseModelInputs class using its add function. We provide the input-output format along with firebase_model_input to the FirebaseModelInterpreter class using the FirebaseModelInputOutputOptions class.
FirebaseModelInterpreter class provides the output in the form of an array of size {1,1} containing value- 1 or 0 which indicates if the input text has positive or negative sentiments.


## Tools and Libraries
* Android Studio
* Tensorflow
* Firebase MLkit: https://firebase.google.com/docs/ml-kit
* Android-Image-Cropper: https://github.com/ArthurHub/Android-Image-Cropper
* Picasso: https://github.com/square/picasso

## Developers
Abhilash Rath: [LinkedIn](https://www.linkedin.com/in/therath/) | [Github](https://github.com/AbhilashRath) <br />
Aashish Malik: [LinkedIn](https://www.linkedin.com/in/aashish-malik-615189158/) | [Github](https://github.com/aashishmalik7936)
