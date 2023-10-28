import flask
from flask import request
import cv2
import base64
import numpy as np
import itertools
from numba import njit

app = flask.Flask(__name__)
img_size = 0
img1=""
#to display the connection status
@app.route('/', methods=['GET'])
def handle_call():
    print("connect")
    return "Successfully Connected"

#GETのほうはいらないや
#POSTのほうで処理もやってしまうぞ
#the get method. when we call this, it just return the text "Hey!! I'm the fact you got!!!"
@app.route('/img/get', methods=['GET'])
def get_fact():
    #処理結果をAndroid側に送る
    print("getfact")
    return "I got img_size = "+str(img)

    #return str(img_size)

#the post method. when we call this with a string containing a name, it will return the name with the text "I got your name"
@app.route('/img/post', methods=['POST'])
def extract_img():
    print(request.url)
    img1 = request.form["img1"]
    img2 = request.form["img2"]

    #ここを画像にしてみる
    #ここでは値を得るだけ
    #print("img中身は",str(img1))
    #print("img中身は",str(img2))

    print("decode")
    decode_img1 =decode(img1)
    decode_img2 =decode(img2)


    print("procedure")
    q_img=resize(decode_img1)
    t_img=resize(decode_img2)
    print("main")
    print("HSV")
    
    q_hsv,q_d_img,q_array,vq_array=decreaseColor(q_img)
    q_hist=np.histogram(q_array)

    t_hsv,t_d_img,t_array,vt_array=decreaseColor(t_img)
    t_hist=np.histogram(t_array)

    #ヒストグラム型変換->npArray
    q_array = np.array(list(q_array), np.float32)
    t_array = np.array(list(t_array), np.float32)
    
    vq_array = np.array(list(vq_array), np.float32)
    vt_array = np.array(list(vt_array), np.float32)
    
    c_result=cv2.compareHist(q_array, t_array, cv2.HISTCMP_BHATTACHARYYA)
    print("Hの類似度は",c_result)
    v_result=cv2.compareHist(vq_array, vt_array, cv2.HISTCMP_BHATTACHARYYA)
    print("Vの類似度は",v_result)
    
    mix_result = (16*c_result +4*v_result)/20
    print(mix_result)
   
    mix_result_score =culcScore(0.20, mix_result)
    
    print("総得点は",round(mix_result_score))


    #img_size = test(decode_img)
    return "得点は"+str(mix_result_score)+"点！！"; #str(img);#str(img_size);

def decode(data1):
    #data1
    #base64型から読めるString型へ
    data1 += "" * ((4 - len(data1) % 4) % 4)  
    #print(data1)
    decode_data1= base64.urlsafe_b64decode(data1)
    #np配列に変換
    np_data1 = np.fromstring(decode_data1,np.uint8)
    #Arrayを読み込んでimgにdecode
    img1 = cv2.imdecode(np_data1,cv2.IMREAD_UNCHANGED)
    #print(img1)
    return img1

def resize(img):
    height = img.shape[0]
    width = img.shape[1]
    w=500
    h=round(w/width*height)
    dst = cv2.resize(img,dsize=(w,h))
    return dst

def decreaseColor(img):
    #hsv = rgb_to_hsv(img)
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    h,s,v = cv2.split(hsv)
    
    #平滑化
    c=40
    m=128
    #v = (v-np.mean(v)) / np.std(v) * c + m
    #v = np.array(v, dtype=np.uint8) # 配列のdtypeをunit8に戻す
    #plt.hist(v.ravel(),256,[0,256]);plt.show()
    #print(v)
    
    #s = (s-np.mean(s)) / np.std(s) * c + m
    #s = np.array(s, dtype=np.uint8) # 配列のdtypeをunit8に戻す
    #plt.hist(s.ravel(),256,[0,256]);plt.show()
    #print(s)
    
    
    idx = np.where((0<=h) & (h<8))
    h[idx] =0
    idx = np.where((8<=h) & (h<26))
    h[idx] =17
    idx = np.where((26<=h) & (h<42))
    h[idx] =34
    idx = np.where((42<=h) & (h<58))
    h[idx] =51
    idx = np.where((58<=h) & (h<74))
    h[idx] =68
    idx = np.where((74<=h) & (h<90))
    h[idx] =85
    idx = np.where((90<=h) & (h<106))
    h[idx] =102
    idx = np.where((106<=h) & (h<122))
    h[idx] =119
    idx = np.where((122<=h) & (h<138))
    h[idx] =130
    idx = np.where((138<=h) & (h<154))
    h[idx] =146
    idx = np.where((154<=h) & (h<170))
    h[idx] =162
    idx = np.where((170<=h) & (h<186))
    h[idx] =178
    idx = np.where((186<=h) & (h<202))
    h[idx] =194
    idx = np.where((202<=h) & (h<218))
    h[idx] =210
    idx = np.where((218<=h) & (h<234))
    h[idx] =226
    idx = np.where((234<=h) & (h<250))
    h[idx] =242
    idx = np.where((250<=h) & (h<255))
    h[idx] =255

    h[idx] = np.round(h[idx]*(15/256))
    idx = np.where((0<=s) & (s<43))    
    s[idx] = 0
    v[idx] = 0
    idx = np.where((43<=s) & (s<128))    
    s[idx] = 85
    v[idx] = 85
    idx = np.where((128<=s) & (s<213))    
    s[idx] = 170
    v[idx] = 170
    idx = np.where((213<=s) & (s<255))   
    s[idx] = 255
    v[idx] = 255

    hh=list(itertools.chain.from_iterable(h))

    ss=list(itertools.chain.from_iterable(s))
    vv=list(itertools.chain.from_iterable(v))

    img_size = img.shape[0]*img.shape[1]
    c_hist_array = np.zeros(256)
    v_hist_array = np.zeros(256)
    for idx in range(img_size):
        s_thre = 255 - 0.8*vv[idx]/255
        if ss[idx] >= 126:#64:
            c_hist_array[hh[idx]] = c_hist_array[hh[idx]]+1
            #print(ss[idx])
        elif ss[idx] < 126:#64:
            v_hist_array[vv[idx]] = v_hist_array[vv[idx]]+1
            #print(ss[idx2])
    hsvs=cv2.merge((h,s,v))
    img = cv2.cvtColor(img, cv2.COLOR_HSV2BGR)
    return hsvs,img,c_hist_array,v_hist_array

def culcScore(color_thres:float,color_score:float):
    thres_score = 60
    zero_border = 0.45
    
    if color_score < color_thres and color_score >= 0:
        color_result = -((thres_score-100)/-color_thres**2)*color_score**2+100
    elif color_score > color_thres and color_score <= zero_border:
        color_result =(thres_score/(color_thres-zero_border)**2)* (color_score-zero_border)**2
    else:
        color_result = 0
    #color_result=round(color_result,2)
    #char_result=round(char_result,2)
    return color_result
'''
@app.route('/getname/<name>', methods=['POST'])
def extract_name(name):
    #ここを画像にしてみる
    
    return "I got your name "+name;
'''

#this commands the script to run in the given port

if __name__ == '__main__':
    #Flaskサーバが実行される
    #"http://〇.〇.〇.〇:5000/"という場所にサーバが作られる
    app.run(host="0.0.0.0", port=5000, debug=True)
    #0の部分をいじれば任意のサーバの場所を指定できる？



