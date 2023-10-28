import numpy as np
import cv2
import glob
from skimage import feature
from numba import njit
import itertools
import base64
import matplotlib.pyplot as plt
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
    #print(hsvs)
    #print(h)
    h[idx] = np.round(h[idx]*(15/256))
    #h[idx] = h[idx]*(256/15)
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
    #s[idx] = np.round(s[idx]*(3/256),decimals=0)
    #print(s)
    #s[idx] = s[idx]*(256/3)
    
    #v[idx] = np.round(v[idx]*(3/256))
    #v[idx] = np.round(v[idx]*(15/180))
    #v[idx] = v[idx]*(256/3)
    #print(v)
    
    #print("返還後",h)
    #print("返還後",s)
    #print("返還後",v)
    hh=list(itertools.chain.from_iterable(h))
    #hh=np.ravel(h).copy
    ss=list(itertools.chain.from_iterable(s))
    vv=list(itertools.chain.from_iterable(v))
    #print("返還後",ss)
    img_size = img.shape[0]*img.shape[1]
    
    #vdx = np.where((0<=v) & (v<4))
    #print(vv)
    c_hist_array = np.zeros(256)
    v_hist_array = np.zeros(256)
    for idx in range(img_size):
        s_thre = 255 - 0.8*vv[idx]/255
        #print(s_thre)
        #print(vv[idx])
        #idx = hh[idx]
        #print(idx)
        #idx2 = vv[idx]
        
        if ss[idx] >= 126:#64:
            c_hist_array[hh[idx]] = c_hist_array[hh[idx]]+1
            #print(ss[idx])
        elif ss[idx] < 126:#64:
            v_hist_array[vv[idx]] = v_hist_array[vv[idx]]+1
            #print(ss[idx2])
    hsvs=cv2.merge((h,s,v))
    img = cv2.cvtColor(img, cv2.COLOR_HSV2BGR)
    return hsvs,img,c_hist_array,v_hist_array

@njit
def listAppend(dic):
    l=list()
    i=0
    #imgsize=img.shape[0]*img.shape[1]
    for i in range(512):
        #dic[i]=dic[i]/imgsize
        l.append(dic[i])
        i=i+1
    return l

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


def main(data1,data2):
    #data1
    #base64型から読めるString型へ
    decode_data1= base64.b64decode(data1)
    #np配列に変換
    np_data1 = np.fromstring(decode_data1,np.uint8)
    #Arrayを読み込んでimgにdecode
    img1 = cv2.imdecode(np_data1,cv2.IMREAD_UNCHANGED)

    #data2
    decode_data2 = base64.b64decode(data2)
    #np配列に変換
    np_data2 = np.fromstring(decode_data2,np.uint8)
    #Arrayを読み込んでimgにdecode
    img2 = cv2.imdecode(np_data2,cv2.IMREAD_UNCHANGED)

    q_img=resize(img1)
    t_img=resize(img2)
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
    
    print("____________________________________________")
    result = culcScore(0.20,c_result)
    result2 = culcScore(0.20,v_result)
    print("色相の得点は",round(result))
    print("白黒の得点は",round(result2))
    return str(round(mix_result_score))
    
if __name__ =='__main__':
    q_img=resize(q_img)
    t_img=resize(t_img)
    print("main")
    print("HSV")
    
    q_hsv,q_d_img,q_array,vq_array=decreaseColor(q_img)
    q_hist=np.histogram(q_array)

    t_hsv,t_d_img,t_array,vt_array=decreaseColor(t_img)
    t_hist=np.histogram(t_array)
    
    print("make_q_Hist")
    makeHist(q_array)
    v_makeHist(vq_array)
    print("make_t_Hist")
    makeHist(t_array)
    v_makeHist(vt_array)
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
    
    print("____________________________________________")
    result = culcScore(0.20,c_result)
    result2 = culcScore(0.20,v_result)
    print("色相の得点は",round(result))
    print("白黒の得点は",round(result2))
    