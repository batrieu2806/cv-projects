/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/cppFiles/file.h to edit this template
 */

/* 
 * File:   dataloader.h
 * Author: ltsach
 *
 * Created on September 2, 2024, 4:01 PM
 */

#ifndef DATALOADER_H
#define DATALOADER_H
#include "tensor/xtensor_lib.h"
#include "loader/dataset.h"

using namespace std;

template<typename DType, typename LType>
class DataLoader{
public:
    
private:
    Dataset<DType, LType>* ptr_dataset;
    int batch_size;
    bool shuffle;
    bool drop_last;
    int seed;
    /*TODO: add more member variables to support the iteration*/
    xt::xarray<size_t> arrange;
    size_t numBatch;
public:
    DataLoader(Dataset<DType, LType>* ptr_dataset,
        int batch_size,
        bool shuffle=true,
        bool drop_last=false,
        int seed = -1){
        /*TODO: Add your code to do the initialization */
        this->ptr_dataset = ptr_dataset;
        this->batch_size = batch_size;
        this->shuffle = shuffle;
        this->drop_last = drop_last;
        this->seed = seed;

        size_t datasetLength = static_cast<size_t>(ptr_dataset->len());
        arrange = xt::arange<size_t>(datasetLength);

        if (shuffle) {
            if (seed >= 0) {
                xt::random::seed(seed); 
            }
            xt::random::shuffle(arrange);
            //cout << "Shuffled indices: " << arrange << std::endl;
        }
        if (drop_last) {
            numBatch = datasetLength / batch_size;
        } else {
            numBatch = datasetLength / batch_size;
        }
        
    }
    virtual ~DataLoader(){}
    
    /////////////////////////////////////////////////////////////////////////
    // The section for supporting the iteration and for-each to DataLoader //
    /// START: Section                                                     //
    /////////////////////////////////////////////////////////////////////////
    
    /*TODO: Add your code here to support iteration on batch*/
    class Iterator {
    private:
        DataLoader<DType, LType>* loader;
        unsigned long curBatch;

    public:
        Iterator(DataLoader<DType, LType>* loader, unsigned long curBatch) {
            this->loader = loader;
            this->curBatch = curBatch;
        }

        bool operator!=(const Iterator& other) const {
            return this->curBatch != other.curBatch;
        }
        

        Batch<DType, LType> operator*() {
            int startIdx = curBatch * loader->batch_size;
            int endIdx = std::min(startIdx + loader->batch_size,static_cast<int>(loader->arrange.size()));
    
            // If the next batch would be smaller than the batch size, merge it into the current batch
            if ((loader->arrange.size() - endIdx < loader->batch_size) && !loader->drop_last ) {
                endIdx = loader->arrange.size();  // Include the remaining samples from the next batch
            }
            //cout << startIdx <<" , "<< endIdx<<" , " << endl;
            if (loader->drop_last && (endIdx - startIdx) < loader->batch_size) {
                return Batch<DType, LType>(xt::xarray<DType>(), xt::xarray<LType>());
            }
            bool isLabel = false;
            if  (loader->ptr_dataset->get_label_shape().size() > 0 && loader->ptr_dataset->get_label_shape()[0] > 0){
                isLabel = true;
            }
            DataLabel<DType, LType> samDatalabel = loader->ptr_dataset->getitem(loader->arrange[startIdx]);
            xt::xarray<DType> samData = samDatalabel.getData();
            std::vector<std::size_t> samDataShape(samData.shape().begin(), samData.shape().end());

            std::vector<std::size_t> batchDataShape = { static_cast<std::size_t>(endIdx - startIdx) };
            batchDataShape.insert(batchDataShape.end(),samDataShape.begin(),samDataShape.end());

            xt::xarray<DType> batchData = xt::zeros<DType>(batchDataShape);
            xt::xarray<LType> batchLabel;

            if (isLabel) {
                xt::xarray<LType> samLabel = samDatalabel.getLabel();
                std::vector<std::size_t> samLabelShape(samLabel.shape().begin(), samLabel.shape().end());

                std::vector<std::size_t> batchLabelShape = { static_cast<std::size_t>(endIdx - startIdx) };
                batchLabelShape.insert(batchLabelShape.end(),samLabelShape.begin(),samLabelShape.end());


                batchLabel = xt::zeros<LType>(batchLabelShape);
            }
             for (int i = startIdx; i < endIdx; ++i) {
                //cout<<"Arrange ="<<loader->arrange<<endl<<loader->arrange(i)<<endl;
                int dataIdx = loader->arrange(i);
                DataLabel<DType, LType> datalabel = loader->ptr_dataset->getitem(dataIdx);
                xt::view(batchData, i - startIdx) = datalabel.getData();
                if (isLabel) {
                    xt::view(batchLabel, i - startIdx) = datalabel.getLabel();
                }
            }

            return Batch<DType, LType>(batchData, batchLabel);
        }
        Iterator& operator++() {
            ++curBatch;
            return *this;
        }

        Iterator operator++(int) {
            Iterator temp = *this;
            ++(*this);
            return temp;
        }
    };

    Iterator begin() {
    if (shuffle) {
        if (seed >= 0) {
            //xt::random::seed(seed);  // Set the seed here as well
        }
        //xt::random::shuffle(arrange);
    }
    return Iterator(this, 0);
}

    Iterator end() {
       return Iterator(this, numBatch);
    }
    /////////////////////////////////////////////////////////////////////////
    // The section for supporting the iteration and for-each to DataLoader //
    /// END: Section                                                       //
    /////////////////////////////////////////////////////////////////////////
};


#endif /* DATALOADER_H */

