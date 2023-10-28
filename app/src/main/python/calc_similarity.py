# -*- coding: utf-8 -*-
"""
Created on Mon Jun 27 15:38:55 2022

@author: hiram
"""

# -*- coding: utf-8 -*-
"""
Created on Thu Mar  3 14:38:01 2022

@author: Owner
"""

import cv2
import numpy as np
import matplotlib.pyplot as plt
#from IPython.display import Image, display
#from scipy.stats import kde
import math
import io 
import time
from PIL import Image
import base64
from numba import jit
from numba import njit
#from numba.typed import Dict
#from numba import types
import decimal
#import imutils
t1 = time.time() #時間計測開始


@njit
def img_box(img):    
    ebox=np.zeros(512)
    i=0
    j=0
    n=0
    #keys=(n for n in range(0, len(ebox)))
    w=img.shape[0]
    x=img.shape[1]
    
    for i in range(0,w):
        j=0
        for j in range(0,x):
            elm=img[i,j,:]#imgのRGB値を取得
            box=elm[0]*8*8+elm[1]*8+elm[2]#格納する場所を決める
            ebox[box]=ebox[box]+1
    return ebox




#分けてヒストグラム作っては意味がないのか!
#その画素のRGBを取得しなければならない
#@jit
#@njit
def decreaseColor(img):#こっちに問題ある説？
#この処理にはさほど時間がかからない
  dst = img.copy()
  idx = np.where((0<=img) & (img<256))
  #print(idx)
  #print(dst[idx])
  dst[idx] = dst[idx]//32
  return dst
#@jit('（戻り値の型）(（引数1の型）,（引数2の型）,…)', nopython=True)

def makeHist(dic):
    vals=listAppend(dic)
    keys=list(i for i in range(512))
    #正規化したい
    hist=plt.bar(keys,vals)
    plt.title('histgram', fontsize=20)  # タイトル
    plt.ylabel ('amount')
    plt.xlabel ('box_number')
    plt.show()
    return

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
   
@njit
#バタチャリヤを用いたヒストグラム比較の式
def compareHist(d1,d2,img_size):#h1はヒストグラムデータ
    l1=list(d1.values())
    l2=list(d2.values())
    latter=0
    i=0
    m_l1=0
    m_l2=0
    for i in range(512):
        l1[i]=l1[i]/img_size
        l2[i]=l2[i]/img_size
        rst = math.sqrt(l1[i]*l2[i])
        latter +=rst
        m_l1 += l1[i]
        m_l2 += l2[i]
    #OpenCVのcapareHistのコードとか見てみるか
    m_l1 = m_l1/512
    m_l2 = m_l2/512
    former = (1/math.sqrt(m_l1*m_l2*512*512))#listの掛け算はできない
    result=math.sqrt(1-former*latter)
    #result=result/img_size
    latter = (1-latter)
    print('bhttacharyya?? : ',latter) #bhattacharyya係数だけ　１から引いてる
    
    
    print('bhattacharyya : ',result)#opencvのリファレンスの式
    return result

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
#手直ししないと使えない
def mean( hist ):
    mean = 0.0;
    for i in hist:
        mean += i;
    mean/= len(hist);
    return mean;

def bhatta ( hist1,  hist2):
    # calculate mean of hist1
    h1_ = mean(hist1);

    # calculate mean of hist2
    h2_ = mean(hist2);

    # calculate score
    score = 0;
    for i in range(512):
        score += math.sqrt( hist1[i] * hist2[i] );
    # print h1_,h2_,score;
    score = math.sqrt( 1 - ( 1 / math.sqrt(h1_*h2_*8*8) ) * score );
    return score;
'''

#target=cv2.imread('the_study/aed01.png')
#target=cv2.imread('the_study/crecore01.jpg')
target=cv2.imread('the_study/obj02.jpg')

#src=cv2.imread('the_study/crecore03.jpg')
#src=cv2.imread('the_study/stone01.jpg')
#src=cv2.imread('the_study/rainbow1.jpg')
src=cv2.imread('the_study/obj05.jpg')


#@jit
def main(data1,data2):
    #base64型からimageへdecodeする

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

    #_____________________________________________________________
    #ここから一般的な処理
    #OpenCV処理部分
    target=decreaseColor(img1)
    src=decreaseColor(img2)
    #ヒストグラム作成
    t_l=img_box(target)
    s_l=img_box(src)
    
    t_val=listAppend(t_l)    
    s_val=listAppend(s_l)
    
    #型変換　->ndarray float32
    t_val2 = np.array(list(t_val), np.float32)
    s_val2 = np.array(list(s_val), np.float32)
    result=cv2.compareHist(t_val2, s_val2, cv2.HISTCMP_BHATTACHARYYA)
    result_score =culcScore(0.20, result)

    return str(round(result_score))


if __name__ =='__main__':
    target=decreaseColor(target)
    src=decreaseColor(src)
    
    t_l=img_box(target)
    s_l=img_box(src)
    
    t_val=listAppend(t_l)    
    s_val=listAppend(s_l)
    
    #ヒストグラム型変換->npArray
    t_val2 = np.array(list(t_val), np.float32)
    s_val2 = np.array(list(s_val), np.float32)
   
    print(t_val2.shape)
    print(t_val2.dtype)
    #print(t_val2)
    #print(t_val2)
    
    #ヒストグラム比較　Bhattacharyya
    result=cv2.compareHist(t_val2, s_val2, cv2.HISTCMP_BHATTACHARYYA)
    print("類似度：",round(result,3))
    #sim = cv2.compareHist(t_hist, s_hist, 3)
    t2=time.time()
    print("処理時間 = ",t2-t1)
    
    imgSize=target.shape[0]*target.shape[1]
    print("画素数",imgSize)
    
    imgS=0
    n=0
